import useAuth from '../../hooks/useAuth.js'
import './ProfilePage.css'

function ProfilePage() {
  const { client } = useAuth()
  const fullName = [client.name, client.paternalSurname, client.maternalSurname].filter(Boolean).join(' ')

  return (
    <div className="profile-page page-container">
      <header><p className="eyebrow">Perfil</p><h1>Datos de tu cuenta</h1><p>Esta información identifica la sesión con la que administras tus operaciones.</p></header>
      <section aria-labelledby="profile-personal-title">
        <div className="profile-page__identity">
          {client.photoUrl ? <img src={client.photoUrl} alt="Fotografía de perfil" /> : <span aria-hidden="true">{client.name.charAt(0)}</span>}
          <div><h2 id="profile-personal-title">{fullName}</h2><p>Cliente de Virtual Insurance</p></div>
        </div>
        <dl><div><dt>Nombre completo</dt><dd>{fullName}</dd></div><div><dt>Correo electrónico</dt><dd>{client.email}</dd></div></dl>
        <p className="profile-page__note">La edición de datos personales se habilitará cuando exista un proceso de actualización y validación en la API.</p>
      </section>
    </div>
  )
}

export default ProfilePage
