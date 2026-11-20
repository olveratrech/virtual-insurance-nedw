import '../authForms.css'

function RegisterForm() {
  return (
    <section className="auth-form auth-form--unavailable" aria-labelledby="register-title">
      <div>
        <h2 id="register-title">Crear cuenta</h2>
        <p>El registro ampliado se conectará en el siguiente incremento.</p>
      </div>
      <p>
        El backend ya admite apellidos, fecha de nacimiento, sexo, género, identificador fiscal
        y fotografía. El formulario se actualizará para solicitar esos datos correctamente.
      </p>
      <button className="button button--secondary auth-form__submit" type="button" disabled>
        Registro en preparación
      </button>
    </section>
  )
}

export default RegisterForm
