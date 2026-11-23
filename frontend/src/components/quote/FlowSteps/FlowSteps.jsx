import './FlowSteps.css'

const steps = ['Cotización', 'Propuesta', 'Pago']

function FlowSteps({ current }) {
  return (
    <nav className="flow-steps" aria-label="Progreso de contratación">
      <ol>
        {steps.map((step, index) => {
          const number = index + 1
          const state = number < current ? 'complete' : number === current ? 'current' : 'pending'
          return (
            <li key={step} className={`flow-steps__item flow-steps__item--${state}`} aria-current={state === 'current' ? 'step' : undefined}>
              <span>{number}</span><strong>{step}</strong>
            </li>
          )
        })}
      </ol>
    </nav>
  )
}

export default FlowSteps
