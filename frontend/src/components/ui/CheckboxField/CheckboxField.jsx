import './CheckboxField.css'

function CheckboxField({ id, label, description, ...inputProps }) {
  return (
    <div className="checkbox-control">
      <input id={id} type="checkbox" {...inputProps} />
      <label htmlFor={id}>
        <strong>{label}</strong>
        {description && <span>{description}</span>}
      </label>
    </div>
  )
}

export default CheckboxField
