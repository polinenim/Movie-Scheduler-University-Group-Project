import { useState } from 'react'

export default function SearchBar({ onSearch, onClear }) {
  const [query, setQuery] = useState('')

  function handleChange(e) {
    const val = e.target.value
    setQuery(val)
  }

  function handleSearch() {
    if (query.length > 2) {
      onSearch(query)
    }
  }
    function handleClear() {
    setQuery('')
    if (onClear) {
      onClear()
    }
  }

  function search(e) {
    if (query.length > 2 && e.key === "Enter") {
      onSearch(query)
    }
  }


  return (
    <div className="flex gap-2 w-full max-w-md">
      <input
        type="text"
        placeholder="Search for a movie..."
        value={query}
        onChange={handleChange}
        onKeyDown={search}
        className="border p-2 rounded flex-1"
      />
      <button
        onClick={handleSearch}
        disabled={query.length <= 2}
        className="bg-blue-500 hover:bg-blue-600 disabled:bg-gray-400 text-white px-4 py-2 rounded"
      >
        Search
      </button>
      <button
        onClick={handleClear}
        className="bg-gray-400 hover:bg-gray-500 text-white px-4 py-2 rounded"
      >
        Clear
      </button>
    </div>
  )
}