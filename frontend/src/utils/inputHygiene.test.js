import { describe, expect, it } from 'vitest'
import {
  formatCardNumber,
  formatExpiration,
  sanitizeAlphabetic,
  sanitizeIdentifier,
  sanitizeNumeric,
} from './inputHygiene.js'

describe('inputHygiene', () => {
  it('elimina números y símbolos de los nombres sin perder acentos', () => {
    expect(sanitizeAlphabetic('María José 2026 @UNAM')).toBe('María José UNAM')
  })

  it('acepta solamente dígitos y respeta la longitud máxima', () => {
    expect(sanitizeNumeric('55a 1234-56789', 10)).toBe('5512345678')
  })

  it('normaliza identificadores a mayúsculas y conserva el ampersand fiscal', () => {
    expect(sanitizeIdentifier('abc&010203xy-!', 13)).toBe('ABC&010203XY-')
  })

  it('formatea una tarjeta en bloques sin exceder sus dígitos', () => {
    expect(formatCardNumber('4242a4242-4242 424299', 16)).toBe('4242 4242 4242 4242')
  })

  it('inserta automáticamente la diagonal de vencimiento', () => {
    expect(formatExpiration('8')).toBe('08/')
    expect(formatExpiration('0829')).toBe('08/29')
    expect(formatExpiration('12/30')).toBe('12/30')
  })
})
