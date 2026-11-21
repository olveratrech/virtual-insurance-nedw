import './DocumentList.css'

function DocumentList({ documents, onDownload, downloadingId }) {
  return (
    <section className="document-section" aria-labelledby="document-list-title">
      <h2 id="document-list-title">Documentos</h2>
      <ul className="document-list">
        {documents.map((document) => (
          <li key={document.id}>
            <div><strong>{document.name}</strong><span>{document.format}</span></div>
            <button
              className="button button--secondary"
              type="button"
              disabled={downloadingId === document.id}
              onClick={() => onDownload(document)}
            >
              {downloadingId === document.id ? 'Descargando…' : 'Descargar'}
            </button>
          </li>
        ))}
      </ul>
    </section>
  )
}

export default DocumentList
