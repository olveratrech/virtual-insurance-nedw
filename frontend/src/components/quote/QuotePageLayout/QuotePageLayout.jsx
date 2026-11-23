import './QuotePageLayout.css'
import FlowSteps from '../FlowSteps/FlowSteps.jsx'

function QuotePageLayout({ eyebrow, title, description, children }) {
  return (
    <div className="quote-page page-container">
      <FlowSteps current={1} />
      <header className="quote-page__heading">
        <p className="eyebrow">{eyebrow}</p>
        <h1>{title}</h1>
        <p>{description}</p>
      </header>
      <div className="quote-page__form-card">{children}</div>
    </div>
  )
}

export default QuotePageLayout
