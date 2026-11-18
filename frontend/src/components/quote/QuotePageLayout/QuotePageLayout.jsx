import './QuotePageLayout.css'

function QuotePageLayout({ eyebrow, title, description, children }) {
  return (
    <div className="quote-page page-container">
      <header className="quote-page__heading">
        <p className="eyebrow">{eyebrow}</p>
        <h1>{title}</h1>
        <p>{description}</p>
      </header>
      <aside className="prototype-notice" role="note">
        Evaluación demostrativa. Las reglas definitivas se ejecutarán posteriormente en Spring Boot.
      </aside>
      <div className="quote-page__form-card">{children}</div>
    </div>
  )
}

export default QuotePageLayout
