import { Link } from 'react-router-dom'
import InsuranceCard from '../../components/ui/InsuranceCard/InsuranceCard.jsx'
import './HomePage.css'

function HomePage() {
  return (
    <div className="home-page page-container">
      <section className="home-hero">
        <p className="eyebrow">Seguros claros, protección real</p>
        <h1>Protege lo que más importa</h1>
        <p className="home-hero__description">
          Cotiza un seguro de auto o médico en minutos, conoce el precio antes
          de contratar y administra tus pólizas desde un solo lugar.
        </p>
        <div className="home-hero__actions">
          <Link className="button button--primary" to="/cotizar/auto">Cotizar ahora</Link>
          <Link className="button button--secondary" to="/acceso">Iniciar sesión</Link>
        </div>
      </section>

      <section aria-labelledby="insurance-options-title">
        <div className="section-heading">
          <p className="eyebrow">Opciones de cobertura</p>
          <h2 id="insurance-options-title">Elige el seguro que necesitas</h2>
        </div>
        <div className="insurance-grid">
          <InsuranceCard title="Seguro de auto" description="Protección personalizada para tu vehículo y tu forma de conducir." destination="/cotizar/auto" actionLabel="Cotizar auto" />
          <InsuranceCard title="Seguro médico" description="Cobertura calculada con la información y necesidades del asegurado." destination="/cotizar/medico" actionLabel="Cotizar seguro médico" />
        </div>
      </section>
    </div>
  )
}

export default HomePage
