import { useEffect, useState } from 'react'
import CheckboxField from '../../ui/CheckboxField/CheckboxField.jsx'
import FormField from '../../ui/FormField/FormField.jsx'
import SelectField from '../../ui/SelectField/SelectField.jsx'
import { getGenders } from '../../../services/catalogApi.js'
import '../authForms.css'

const MAX_PHOTO_SIZE = 2 * 1024 * 1024
const SUPPORTED_PHOTO_TYPES = new Set(['image/jpeg', 'image/png'])

const initialValues = {
  name: '',
  paternalSurname: '',
  maternalSurname: '',
  birthDate: '',
  sex: '',
  genderId: '',
  fiscalIdentifier: '',
  email: '',
  password: '',
  passwordConfirmation: '',
  acceptsTerms: false,
}

function latestAdultBirthDate() {
  const today = new Date()
  const year = today.getFullYear() - 18
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function RegisterForm({ onSuccess }) {
  const [values, setValues] = useState(initialValues)
  const [photo, setPhoto] = useState(null)
  const [genders, setGenders] = useState([])
  const [catalogStatus, setCatalogStatus] = useState('loading')
  const [errors, setErrors] = useState({})
  const [submitError, setSubmitError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function loadGenders() {
    setCatalogStatus('loading')
    try {
      setGenders(await getGenders())
      setCatalogStatus('ready')
    } catch {
      setGenders([])
      setCatalogStatus('error')
    }
  }

  useEffect(() => {
    let active = true

    getGenders()
      .then((items) => {
        if (!active) return
        setGenders(items)
        setCatalogStatus('ready')
      })
      .catch(() => {
        if (!active) return
        setCatalogStatus('error')
      })

    return () => {
      active = false
    }
  }, [])

  function updateField(event) {
    const { checked, name, type, value } = event.target
    setValues((currentValues) => ({
      ...currentValues,
      [name]: type === 'checkbox' ? checked : value,
    }))
    setErrors((currentErrors) => ({ ...currentErrors, [name]: undefined }))
  }

  function updatePhoto(event) {
    const selectedPhoto = event.target.files?.[0] ?? null
    setPhoto(selectedPhoto)
    setErrors((currentErrors) => ({ ...currentErrors, photo: undefined }))
  }

  function validate() {
    const nextErrors = {}

    if (!values.name.trim()) nextErrors.name = 'Ingresa tu nombre.'
    if (!values.paternalSurname.trim()) nextErrors.paternalSurname = 'Ingresa tu apellido paterno.'
    if (!values.birthDate) {
      nextErrors.birthDate = 'Ingresa tu fecha de nacimiento.'
    } else if (values.birthDate < '1900-01-01') {
      nextErrors.birthDate = 'La fecha de nacimiento no puede ser anterior a 1900.'
    } else if (values.birthDate > latestAdultBirthDate()) {
      nextErrors.birthDate = 'Debes ser mayor de edad.'
    }
    if (!values.sex) nextErrors.sex = 'Selecciona una opción.'
    if (!values.fiscalIdentifier.trim()) nextErrors.fiscalIdentifier = 'Ingresa tu identificador fiscal.'
    if (!/^\S+@\S+\.\S+$/.test(values.email.trim())) {
      nextErrors.email = 'Ingresa un correo electrónico válido.'
    }
    if (values.password.length < 8) nextErrors.password = 'Utiliza al menos 8 caracteres.'
    if (values.password !== values.passwordConfirmation) {
      nextErrors.passwordConfirmation = 'Las contraseñas no coinciden.'
    }
    if (!values.acceptsTerms) {
      nextErrors.acceptsTerms = 'Debes aceptar los términos para continuar.'
    }
    if (photo && !SUPPORTED_PHOTO_TYPES.has(photo.type)) {
      nextErrors.photo = 'La fotografía debe ser JPEG o PNG.'
    } else if (photo && photo.size > MAX_PHOTO_SIZE) {
      nextErrors.photo = 'La fotografía no debe superar 2 MiB.'
    }

    return nextErrors
  }

  async function handleSubmit(event) {
    event.preventDefault()

    const nextErrors = validate()
    setErrors(nextErrors)
    setSubmitError('')
    if (Object.keys(nextErrors).length > 0) return

    const data = {
      name: values.name.trim(),
      paternalSurname: values.paternalSurname.trim(),
      maternalSurname: values.maternalSurname.trim() || null,
      birthDate: values.birthDate,
      sex: values.sex,
      genderId: values.genderId ? Number(values.genderId) : null,
      fiscalIdentifier: values.fiscalIdentifier.trim(),
      email: values.email.trim(),
      password: values.password,
      acceptsTerms: values.acceptsTerms,
    }

    setIsSubmitting(true)
    try {
      await onSuccess(data, photo)
    } catch (error) {
      const fieldErrors = error.problem?.fieldErrors ?? []
      setErrors((currentErrors) => ({
        ...currentErrors,
        ...Object.fromEntries(fieldErrors.map(({ field, message }) => [field, message])),
      }))
      setSubmitError(error.message)
      setValues((currentValues) => ({
        ...currentValues,
        password: '',
        passwordConfirmation: '',
      }))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <form className="auth-form auth-form--register" onSubmit={handleSubmit} noValidate>
      <div>
        <h2>Crear cuenta</h2>
        <p>Registra tus datos para guardar cotizaciones y administrar tus seguros.</p>
      </div>

      <div className="auth-form__grid">
        <FormField id="register-name" label="Nombre" name="name" maxLength="40" autoComplete="given-name" value={values.name} onChange={updateField} error={errors.name} />
        <FormField id="register-paternal-surname" label="Apellido paterno" name="paternalSurname" maxLength="40" autoComplete="family-name" value={values.paternalSurname} onChange={updateField} error={errors.paternalSurname} />
        <FormField id="register-maternal-surname" label="Apellido materno (opcional)" name="maternalSurname" maxLength="40" value={values.maternalSurname} onChange={updateField} error={errors.maternalSurname} />
        <FormField id="register-birth-date" label="Fecha de nacimiento" name="birthDate" type="date" min="1900-01-01" max={latestAdultBirthDate()} value={values.birthDate} onChange={updateField} error={errors.birthDate} />

        <SelectField id="register-sex" label="Sexo" name="sex" value={values.sex} onChange={updateField} error={errors.sex}>
          <option value="">Selecciona una opción</option>
          <option value="M">Masculino</option>
          <option value="F">Femenino</option>
          <option value="X">Otro / no especificado</option>
        </SelectField>

        <SelectField id="register-gender" label="Género (opcional)" name="genderId" value={values.genderId} onChange={updateField} disabled={catalogStatus !== 'ready'} error={errors.genderId}>
          <option value="">Prefiero no seleccionarlo</option>
          {genders.map((gender) => <option key={gender.id} value={gender.id}>{gender.description}</option>)}
        </SelectField>

        <div className="auth-form__full auth-form__catalog-status" aria-live="polite">
          {catalogStatus === 'loading' && <span>Cargando géneros…</span>}
          {catalogStatus === 'error' && (
            <span>
              No fue posible cargar los géneros.{' '}
              <button className="auth-form__inline-action" type="button" onClick={loadGenders}>Reintentar</button>
            </span>
          )}
        </div>

        <FormField id="register-fiscal-identifier" label="Identificador fiscal" name="fiscalIdentifier" maxLength="40" value={values.fiscalIdentifier} onChange={updateField} error={errors.fiscalIdentifier} />
        <FormField id="register-email" label="Correo electrónico" name="email" type="email" maxLength="250" autoComplete="email" value={values.email} onChange={updateField} error={errors.email} />
        <FormField id="register-password" label="Contraseña" name="password" type="password" maxLength="72" autoComplete="new-password" value={values.password} onChange={updateField} error={errors.password} />
        <FormField id="register-password-confirmation" label="Confirmar contraseña" name="passwordConfirmation" type="password" maxLength="72" autoComplete="new-password" value={values.passwordConfirmation} onChange={updateField} error={errors.passwordConfirmation} />

        <div className="auth-form__full">
          <FormField id="register-photo" label="Fotografía (opcional)" name="photo" type="file" accept="image/jpeg,image/png" onChange={updatePhoto} error={errors.photo} />
          <p className="auth-form__hint">JPEG o PNG, máximo 2 MiB. No se almacena ninguna imagen si dejas el campo vacío.</p>
        </div>

        <div className="auth-form__full">
          <CheckboxField
            id="register-accepts-terms"
            label="Acepto los términos y el aviso de privacidad"
            name="acceptsTerms"
            checked={values.acceptsTerms}
            onChange={updateField}
            aria-describedby={errors.acceptsTerms ? 'register-accepts-terms-error' : undefined}
          />
          {errors.acceptsTerms && <p className="form-field__error" id="register-accepts-terms-error">{errors.acceptsTerms}</p>}
        </div>
      </div>

      {submitError && <p className="auth-form__error" role="alert">{submitError}</p>}

      <button className="button button--primary auth-form__submit" type="submit" disabled={isSubmitting}>
        {isSubmitting ? 'Creando cuenta…' : 'Crear cuenta'}
      </button>
    </form>
  )
}

export default RegisterForm
