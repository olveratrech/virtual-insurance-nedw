export const dashboardData = {
  customerName: 'Emmanuel',
  actions: [
    { title: 'Cotizar seguro de auto', description: 'Calcula una cobertura de acuerdo con tu vehículo y antecedentes.', destination: '/cotizar/auto', actionLabel: 'Nueva cotización de auto' },
    { title: 'Cotizar seguro médico', description: 'Conoce una propuesta basada en los datos de salud declarados.', destination: '/cotizar/medico', actionLabel: 'Nueva cotización médica' },
  ],
  recentActivity: [
    { id: 'policy-802145', type: 'Póliza de auto', reference: 'VI-802145', status: 'ACTIVA', statusTone: 'success', detail: 'Vigencia hasta el 25 de noviembre de 2027', destination: '/polizas/802145' },
    { id: 'payment-0001', type: 'Pago mensual', reference: '#0001', status: 'APLICADO', statusTone: 'neutral', detail: '$1,040.00 MXN · 26 de noviembre de 2026', destination: '/polizas/802145' },
  ],
}
