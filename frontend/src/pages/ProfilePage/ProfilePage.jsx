import { useEffect, useState } from 'react'
import FormField from '../../components/ui/FormField/FormField.jsx'
import SelectField from '../../components/ui/SelectField/SelectField.jsx'
import useAuth from '../../hooks/useAuth.js'
import { getGenders } from '../../services/catalogApi.js'
import { deleteProfilePhoto, getProfile, updateProfile, updateProfilePhoto } from '../../services/profileApi.js'
import './ProfilePage.css'

const emptyValues = { name: '', paternalSurname: '', maternalSurname: '', genderId: '', email: '', currentPassword: '' }

function ProfilePage() {
  const { refreshSession } = useAuth()
  const [profile, setProfile] = useState(null)
  const [values, setValues] = useState(emptyValues)
  const [genders, setGenders] = useState([])
  const [status, setStatus] = useState('loading')
  const [error, setError] = useState('')
  const [errors, setErrors] = useState({})
  const [message, setMessage] = useState('')
  const [saving, setSaving] = useState(false)
  const [editing, setEditing] = useState(false)
  const [dirty, setDirty] = useState(false)
  const [photoBusy, setPhotoBusy] = useState(false)
  const [photoVersion, setPhotoVersion] = useState(0)
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let active = true
    Promise.all([getProfile(), getGenders()])
      .then(([profileResponse, genderResponse]) => {
        if (!active) return
        setProfile(profileResponse)
        setGenders(genderResponse)
        setValues({
          name: profileResponse.name,
          paternalSurname: profileResponse.paternalSurname,
          maternalSurname: profileResponse.maternalSurname ?? '',
          genderId: profileResponse.gender?.id?.toString() ?? '',
          email: profileResponse.email,
          currentPassword: '',
        })
        setStatus('ready')
        setError('')
        setEditing(false)
        setDirty(false)
      })
      .catch((requestError) => {
        if (!active) return
        setStatus('error')
        setError(requestError.message)
      })
    return () => { active = false }
  }, [reloadKey])

  function updateField(event) {
    const { name, value } = event.target
    setValues((current) => ({ ...current, [name]: value }))
    setErrors((current) => ({ ...current, [name]: '' }))
    setMessage('')
    setDirty(true)
  }

  function startEditing() {
    setEditing(true)
    setDirty(false)
    setErrors({})
    setError('')
    setMessage('')
  }

  function validate() {
    const next = {}
    if (!values.name.trim()) next.name = 'Escribe tu nombre.'
    if (!values.paternalSurname.trim()) next.paternalSurname = 'Escribe tu apellido paterno.'
    if (!/^\S+@\S+\.\S+$/.test(values.email.trim())) next.email = 'Escribe un correo válido.'
    if (profile && values.email.trim().toLowerCase() !== profile.email && !values.currentPassword) {
      next.currentPassword = 'Confirma tu contraseña para cambiar el correo.'
    }
    return next
  }

  async function handleSubmit(event) {
    event.preventDefault()
    if (!editing || !dirty) return
    const nextErrors = validate()
    setErrors(nextErrors)
    setError('')
    setMessage('')
    if (Object.keys(nextErrors).length > 0) return

    setSaving(true)
    try {
      const response = await updateProfile({
        name: values.name.trim(),
        paternalSurname: values.paternalSurname.trim(),
        maternalSurname: values.maternalSurname.trim() || null,
        genderId: values.genderId ? Number(values.genderId) : null,
        email: values.email.trim(),
        currentPassword: values.currentPassword || null,
      })
      setProfile(response)
      setValues((current) => ({ ...current, email: response.email, currentPassword: '' }))
      await refreshSession()
      setEditing(false)
      setDirty(false)
      setMessage('Tus datos se actualizaron correctamente.')
    } catch (requestError) {
      const fieldErrors = requestError.problem?.fieldErrors ?? []
      setErrors((current) => ({ ...current, ...Object.fromEntries(fieldErrors.map(({ field, message: fieldMessage }) => [field, fieldMessage])) }))
      setError(requestError.message)
      setValues((current) => ({ ...current, currentPassword: '' }))
    } finally {
      setSaving(false)
    }
  }

  async function handlePhoto(event) {
    const photo = event.target.files?.[0]
    event.target.value = ''
    if (!photo) return
    setPhotoBusy(true)
    setError('')
    setMessage('')
    try {
      const response = await updateProfilePhoto(photo)
      setProfile(response)
      setPhotoVersion((value) => value + 1)
      await refreshSession()
      setMessage('Tu fotografía se actualizó correctamente.')
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setPhotoBusy(false)
    }
  }

  async function removePhoto() {
    setPhotoBusy(true)
    setError('')
    setMessage('')
    try {
      const response = await deleteProfilePhoto()
      setProfile(response)
      await refreshSession()
      setMessage('Tu fotografía se eliminó correctamente.')
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setPhotoBusy(false)
    }
  }

  if (status === 'loading') return <div className="profile-page page-container"><p role="status">Consultando perfil…</p></div>
  if (status === 'error') return <div className="profile-page page-container"><div className="profile-page__error" role="alert"><p>{error}</p><button className="button button--primary" type="button" onClick={() => { setStatus('loading'); setReloadKey((value) => value + 1) }}>Reintentar</button></div></div>

  const fullName = [profile.name, profile.paternalSurname, profile.maternalSurname].filter(Boolean).join(' ')
  const sexLabel = { M: 'Masculino', F: 'Femenino', X: 'Otro / no especificado' }[profile.sex] ?? profile.sex

  return (
    <div className="profile-page page-container">
      <header><p className="eyebrow">Perfil</p><h1>Datos de tu cuenta</h1><p>Actualiza tus datos de contacto y la forma en que apareces en tu cuenta.</p></header>
      {message && <p className="profile-page__success" role="status">{message}</p>}
      {error && <p className="profile-page__error" role="alert">{error}</p>}

      <section aria-labelledby="profile-personal-title">
        <div className="profile-page__identity">
          {profile.photoUrl ? <img src={`${profile.photoUrl}?v=${photoVersion}`} alt="Fotografía de perfil" /> : <span aria-hidden="true">{profile.name.charAt(0)}</span>}
          <div><h2 id="profile-personal-title">{fullName}</h2><p>Cliente de Virtual Insurance</p></div>
        </div>
        <div className="profile-page__photo-actions">
          <label className="button button--secondary" htmlFor="profile-photo">{photoBusy ? 'Procesando…' : 'Cambiar fotografía'}</label>
          <input id="profile-photo" type="file" accept="image/jpeg,image/png" disabled={photoBusy} onChange={handlePhoto} />
          {profile.photoUrl && <button className="button button--secondary" type="button" disabled={photoBusy} onClick={removePhoto}>Eliminar fotografía</button>}
        </div>
        <p className="profile-page__hint">JPEG o PNG, máximo 2 MiB.</p>

        <form className="profile-page__form" onSubmit={handleSubmit} noValidate>
          <FormField id="profile-name" label="Nombre" name="name" maxLength="40" autoComplete="given-name" value={values.name} onChange={updateField} readOnly={!editing} error={errors.name} />
          <FormField id="profile-paternal-surname" label="Apellido paterno" name="paternalSurname" maxLength="40" autoComplete="family-name" value={values.paternalSurname} onChange={updateField} readOnly={!editing} error={errors.paternalSurname} />
          <FormField id="profile-maternal-surname" label="Apellido materno (opcional)" name="maternalSurname" maxLength="40" value={values.maternalSurname} onChange={updateField} readOnly={!editing} error={errors.maternalSurname} />
          <SelectField id="profile-gender" label="Género (opcional)" name="genderId" value={values.genderId} onChange={updateField} disabled={!editing} error={errors.genderId}>
            <option value="">Prefiero no seleccionarlo</option>
            {genders.map((gender) => <option key={gender.id} value={gender.id}>{gender.description}</option>)}
          </SelectField>
          <FormField id="profile-email" label="Correo electrónico" name="email" type="email" maxLength="250" autoComplete="email" value={values.email} onChange={updateField} readOnly={!editing} error={errors.email} />
          <FormField id="profile-current-password" label="Contraseña actual para cambiar el correo" name="currentPassword" type="password" maxLength="72" autoComplete="current-password" value={values.currentPassword} onChange={updateField} readOnly={!editing} error={errors.currentPassword} />
          <div className="profile-page__readonly">
            <div><span>Fecha de nacimiento</span><strong>{profile.birthDate}</strong></div>
            <div><span>Sexo registrado</span><strong>{sexLabel}</strong></div>
            <div><span>Identificador fiscal</span><strong>{profile.fiscalIdentifier}</strong></div>
          </div>
          <div className="profile-page__form-actions">
            <button className="button button--secondary" type="button" disabled={saving || (editing && dirty)} onClick={startEditing}>Editar perfil</button>
            <button className="button button--primary" type="submit" disabled={!editing || !dirty || saving}>{saving ? 'Guardando…' : 'Guardar cambios'}</button>
          </div>
        </form>
      </section>
    </div>
  )
}

export default ProfilePage
