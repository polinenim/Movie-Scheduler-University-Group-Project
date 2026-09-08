export async function mockOmdbSearch(query) {
  // simulate latency
  await new Promise((r) => setTimeout(r, 300))

  // fake result set
  return [
    { Title: `${query} 1`, Year: '2020', imdbID: 'tt001' },
    { Title: `${query} 2`, Year: '2021', imdbID: 'tt002' },
    { Title: `${query} 3`, Year: '2022', imdbID: 'tt003' },
  ]
}
