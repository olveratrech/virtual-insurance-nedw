import { Link } from 'react-router-dom'
import './LegalInformationPage.css'

const content = {
  conditions: {
    eyebrow: 'Información contractual',
    title: 'Condiciones Generales',
    paragraphs: [
      'Las condiciones, coberturas, exclusiones, deducibles, coaseguros y sumas aseguradas mostradas en este portal forman parte exclusivamente de una implementación académica.',
      'Ninguna cotización, póliza o documento generado constituye una oferta vinculante, contrato de seguro o garantía de indemnización.',
    ],
  },
  privacy: {
    eyebrow: 'Protección de datos',
    title: 'Aviso de Privacidad',
    paragraphs: [
      'Los datos capturados se utilizan dentro del entorno académico para demostrar flujos de registro, cotización, pago y administración de pólizas.',
      'No deben ingresarse datos bancarios, fiscales o personales reales. Los canales, teléfonos, tarjetas y operaciones presentados son ficticios y no representan servicios financieros reales.',
    ],
  },
}

function LegalInformationPage({ type }) {
  const page = content[type]
  return (
    <div className="legal-information-page page-container">
      <header>
        <p className="eyebrow">{page.eyebrow}</p>
        <h1>{page.title}</h1>
      </header>
      <article>
        {page.paragraphs.map((paragraph) => <p key={paragraph}>{paragraph}</p>)}
        <p><strong>Virtual Insurance · Negocios Electrónicos y Desarrollo Web · Facultad de Ingeniería · UNAM · 2026.</strong></p>
      </article>
      <Link className="button button--secondary" to="/">Volver al inicio</Link>
    </div>
  )
}

export default LegalInformationPage
