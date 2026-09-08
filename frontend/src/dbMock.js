const KEY = 'my_movie_list'

export function getMyList() {
  const raw = sessionStorage.getItem(KEY)
  return raw ? JSON.parse(raw) : []
}

export function addToList(movie) {
  const current = getMyList()
  if (!current.find((m) => m.imdbID === movie.imdbID)) {
    current.push(movie)
    sessionStorage.setItem(KEY, JSON.stringify(current))
  }
}
