import './DocumentList.css'

function DocumentList({ documents, onDownload }) {
  return (
    <section className="document-section" aria-labelledby="document-list-title">
      <h2 id="document-list-title">Documentos</h2>
      <ul className="document-list">
        {documents.map((document) => (
          <li key={document.id}>
            <div><strong>{document.name}</strong><span>{document.format}</span></div>
            <button className="button button--secondary" type="button" onClick={() => onDownload(document)}>Descargar</button>
          </li>
        ))}
      </ul>
    </section>
  )
}

export default DocumentList
