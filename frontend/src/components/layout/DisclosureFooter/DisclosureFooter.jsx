import { Link } from 'react-router-dom'
import './DisclosureFooter.css'

function DisclosureFooter() {
  return (
    <footer className="disclosure-footer">
      <div className="disclosure-footer__content">
        <section className="disclosure-footer__brand" aria-labelledby="footer-brand-title">
          <h2 id="footer-brand-title">Virtual Insurance</h2>
          <p>Portal digital para explorar procesos de cotización, emisión y administración de seguros.</p>
          <span>IMPLEMENTACIÓN ACADÉMICA · 2026</span>
        </section>

        <nav className="disclosure-footer__links" aria-label="Productos y servicios">
          <h2>Seguros y servicios</h2>
          <Link to="/cotizar/medico">Seguros médicos</Link>
          <Link to="/cotizar/auto">Seguros de auto</Link>
          <Link to="/cotizar">Cotizador</Link>
          <Link to="/polizas">Validación de pólizas</Link>
        </nav>

        <section className="disclosure-footer__attention" aria-labelledby="footer-attention-title">
          <h2 id="footer-attention-title">Atención 24/7</h2>
          <a href="tel:8000002026">800-000-2026</a>
          <p>Ciudad de México, México</p>
          <div>
            <Link to="/condiciones-generales">Condiciones Generales</Link>
            <Link to="/aviso-privacidad">Aviso de Privacidad</Link>
          </div>
        </section>
      </div>
      <div className="disclosure-footer__regulatory">
        <p>
          <strong>Implementación académica para la asignatura de Negocios Electrónicos y Desarrollo Web · Facultad de Ingeniería · UNAM · 2026.</strong>{' '}
          Desarrollada por Emmanuel Trejo Olvera. Este sitio no realiza operaciones financieras o aseguradoras reales. Virtual Insurance no es una institución autorizada, supervisada ni registrada ante la CNSF o la CONDUSEF. Todos los productos, folios, pagos, tarjetas, teléfonos y canales de atención mostrados son ficticios.
        </p>
      </div>
    </footer>
  )
}

export default DisclosureFooter
