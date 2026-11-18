import './SelectField.css'

function SelectField({ id, label, error, children, ...selectProps }) {
  const errorId = `${id}-error`

  return (
    <div className="select-field">
      <label htmlFor={id}>{label}</label>
      <select id={id} aria-describedby={error ? errorId : undefined} aria-invalid={Boolean(error)} {...selectProps}>
        {children}
      </select>
      {error && <p className="form-field__error" id={errorId}>{error}</p>}
    </div>
  )
}

export default SelectField
