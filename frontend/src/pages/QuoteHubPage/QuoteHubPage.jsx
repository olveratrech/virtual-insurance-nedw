import DashboardActionCard from '../../components/dashboard/DashboardActionCard/DashboardActionCard.jsx'
import './QuoteHubPage.css'

const quoteOptions = [
  {
    title: 'Seguro de auto',
    description: 'Obtén una propuesta basada en tu vehículo, licencia y antecedentes declarados.',
    destination: '/cotizar/auto',
    actionLabel: 'Cotizar seguro de auto',
  },
  {
    title: 'Seguro médico',
    description: 'Calcula una propuesta de cobertura médica con la información de salud declarada.',
    destination: '/cotizar/medico',
    actionLabel: 'Cotizar seguro médico',
  },
]

function QuoteHubPage() {
  return (
    <div className="quote-hub-page page-container">
      <header>
        <p className="eyebrow">Cotizar</p>
        <h1>Elige el seguro que necesitas</h1>
        <p>Compara una propuesta de acuerdo con el tipo de protección que buscas.</p>
      </header>
      <div className="quote-hub-page__options">
        {quoteOptions.map((option) => <DashboardActionCard key={option.destination} {...option} />)}
      </div>
    </div>
  )
}

export default QuoteHubPage
