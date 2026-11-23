import { Link } from 'react-router-dom'
import './HelpPage.css'

const topics = [
  ['¿Dónde consulto mis cotizaciones?', 'En Cotizaciones encontrarás todas tus propuestas, tanto aprobadas como rechazadas.'],
  ['¿Dónde descargo mi póliza?', 'Abre una póliza desde Pólizas y utiliza la sección Documentos para descargarla.'],
  ['¿Cómo solicito una cancelación?', 'Entra al detalle de una póliza activa y selecciona Solicitar cancelación.'],
  ['¿Qué ocurre con un pago rechazado?', 'El intento permanece en tu historial de Pagos y no se emite ninguna póliza.'],
]

function HelpPage() {
  return (
    <div className="help-page page-container">
      <header><p className="eyebrow">Ayuda</p><h1>¿Cómo podemos orientarte?</h1><p>Consulta las respuestas sobre los recorridos disponibles en Virtual Insurance.</p></header>
      <section className="help-page__support" aria-labelledby="help-support-title">
        <div><p className="eyebrow">Centro de orientación</p><h2 id="help-support-title">Ayuda y siniestros</h2><p>Consulta primero la información disponible. El registro formal de siniestros se incorporará cuando exista el flujo operativo y documental correspondiente.</p></div>
        <Link className="button button--primary" to="/polizas">Consultar mis pólizas</Link>
      </section>
      <section className="help-page__topics" aria-label="Preguntas frecuentes">
        {topics.map(([title, answer]) => <details key={title}><summary>{title}</summary><p>{answer}</p></details>)}
      </section>
      <aside><h2>Accesos rápidos</h2><div><Link className="button button--primary" to="/cotizar">Nueva cotización</Link><Link className="button button--secondary" to="/pagos">Pagos y facturación</Link></div></aside>
    </div>
  )
}

export default HelpPage
