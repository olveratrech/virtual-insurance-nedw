export const quoteResultData = {
  AUTO: {
    quoteNumber: '#0002',
    productName: 'Seguro de auto',
    coverageName: 'Cobertura amplia',
    annualCost: '$12,480.00 MXN',
    monthlyCost: '$1,040.00 MXN',
    benefits: ['Daños materiales', 'Robo total', 'Responsabilidad civil', 'Asistencia vial'],
    rejectionReason: 'Los antecedentes declarados no cumplen las reglas de aceptación para este producto.',
    retryDestination: '/cotizar/auto',
  },
  MEDICAL: {
    quoteNumber: '#0003',
    productName: 'Seguro médico',
    coverageName: 'Cobertura médica individual',
    annualCost: '$18,600.00 MXN',
    monthlyCost: '$1,550.00 MXN',
    benefits: ['Consultas', 'Hospitalización', 'Estudios de diagnóstico', 'Atención de urgencias'],
    rejectionReason: 'La combinación de factores declarados requiere una evaluación distinta a la disponible en este canal.',
    retryDestination: '/cotizar/medico',
  },
}
