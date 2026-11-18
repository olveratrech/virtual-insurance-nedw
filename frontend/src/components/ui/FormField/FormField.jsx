import './FormField.css'

function FormField({ id, label, error, ...inputProps }) {
  const errorId = `${id}-error`

  return (
    <div className="form-field">
      <label htmlFor={id}>{label}</label>
      <input
        id={id}
        aria-describedby={error ? errorId : undefined}
        aria-invalid={Boolean(error)}
        {...inputProps}
      />
      {error && <p className="form-field__error" id={errorId}>{error}</p>}
    </div>
  )
}

export default FormField
