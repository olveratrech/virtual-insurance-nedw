import './TextareaField.css'

function TextareaField({ id, label, error, ...textareaProps }) {
  const errorId = `${id}-error`

  return (
    <div className="textarea-field">
      <label htmlFor={id}>{label}</label>
      <textarea id={id} aria-describedby={error ? errorId : undefined} aria-invalid={Boolean(error)} {...textareaProps} />
      {error && <p className="form-field__error" id={errorId}>{error}</p>}
    </div>
  )
}

export default TextareaField
