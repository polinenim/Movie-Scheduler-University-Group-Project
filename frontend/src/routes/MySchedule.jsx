import { useState, useEffect } from 'react'
import { useLocation } from 'react-router-dom'
import { useGetMyList, useGetMovies } from '../api/list'
import useSchedule from '../hooks/useSchedule'

export default function MySchedule() {
  const { data: watchlist = [], refetch } = useGetMyList()
  const { scheduleMovie, unscheduleMovie } = useSchedule()
  const scheduled = Array.isArray(watchlist) ? watchlist.filter(i => i.scheduled_at) : []
  // fetch OMDB details for scheduled items so we can show titles
  const moviesQuery = useGetMovies(scheduled)
  const combined = (moviesQuery && moviesQuery.data) || []

  const location = useLocation()

  const [editing, setEditing] = useState(null)
  const [date, setDate] = useState('')
  const [time, setTime] = useState('')
  const [ampm, setAmpm] = useState('AM')

  // compute edit title from merged OMDB info when editing
  const editInfo = editing ? combined.find(m => m && (m.omdb_id === editing.omdb_id || m.imdbID === editing.omdb_id || m.id === editing.omdb_id)) : null
  const editTitle = editInfo?.Title || editInfo?.title || editing?.title || editing?.Title || editing?.omdb_id || editing?.id

  function openEdit(item) {
    setEditing(item)
    if (item.scheduled_at) {
      const parts = toLocalParts(item.scheduled_at)
      setDate(parts.date)
      setTime(parts.time)
      setAmpm(parts.ampm)
    } else {
      setDate('')
      setTime('')
      setAmpm('AM')
    }
  }

  // If a `highlight` query param is present, locate that scheduled item, scroll it into view and open the edit modal
  useEffect(() => {
    const qs = new URLSearchParams(location.search)
    const highlight = qs.get('highlight')
    if (!highlight) return
    // Wait until scheduled items are loaded
    if (!scheduled || scheduled.length === 0) return
    const target = scheduled.find(i => {
      const id = i.omdb_id || i.id
      return id === highlight
    })
    if (target) {
      // Do NOT automatically open the edit modal when arriving with ?highlight=
      // Only scroll the highlighted item into view so the user can interact.
      const el = document.getElementById(`schedule-${encodeURIComponent(target.omdb_id || target.id)}`)
      if (el && typeof el.scrollIntoView === 'function') {
        setTimeout(() => el.scrollIntoView({ behavior: 'smooth', block: 'center' }), 100)
      }
    }
  }, [location.search, combined, scheduled])

  function close() { setEditing(null); setDate(''); setTime(''); setAmpm('AM') }

  async function applyEdit(e) {
    e.preventDefault()
    try {
      let iso = null
      if (date && time) {
        let [hh, mm] = time.split(':').map(s => parseInt(s, 10))
        if (ampm === 'PM' && hh < 12) hh += 12
        if (ampm === 'AM' && hh === 12) hh = 0
        const [year, month, day] = date.split('-').map(s => parseInt(s, 10))
        const dt = new Date(year, month - 1, day, hh, mm || 0)
        iso = dt.toISOString()
      }
      await scheduleMovie(editing.omdb_id || editing.id, iso)
      await refetch()
      close()
    } catch (err) {
      console.error(err)
      alert('Failed to apply')
    }
  }

  async function remove(item) {
    try {
      await unscheduleMovie(item.omdb_id || item.id)
      await refetch()
    } catch (err) {
      console.error(err)
      alert('Failed to remove')
    }
  }

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <h1 className="text-2xl font-bold mb-4">My Schedule</h1>

      {scheduled.length === 0 && <p className="text-gray-500">No scheduled movies</p>}

      <ul className="space-y-3">
        {scheduled.map(item => {
          // find the merged OMDB info for this watchlist entry
          const info = combined.find(m => m && (m.omdb_id === item.omdb_id || m.imdbID === item.omdb_id || m.id === item.omdb_id)) || null
          const title = info?.Title || info?.title || item.title || item.Title || item.omdb_id || item.id
          const displayTime = formatScheduled(item.scheduled_at)
          return (
          <li id={`schedule-${encodeURIComponent(item.omdb_id || item.id)}`} key={item.id || item.omdb_id} className="p-3 border rounded flex justify-between items-center">
            <div>
              <div className="font-semibold">{title}</div>
              <div className="text-sm text-gray-600">{displayTime}</div>
            </div>
            <div className="flex gap-2">
              <button onClick={() => openEdit(item)} className="px-2 py-1 bg-yellow-500 text-white rounded text-xs">Modify</button>
              <button onClick={() => remove(item)} className="px-2 py-1 bg-red-600 text-white rounded text-xs">Remove</button>
            </div>
          </li>
        )})}
      </ul>

      {editing && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50" onClick={close}>
          <form onSubmit={applyEdit} onClick={e => e.stopPropagation()} className="bg-white p-6 rounded shadow w-96">
            <h2 className="font-bold mb-2">Modify: {editTitle}</h2>
            <div className="mb-4 grid grid-cols-3 gap-2 items-end">
              <div className="col-span-2">
                <label className="block text-sm mb-1">Date</label>
                <input type="date" value={date} onChange={e => setDate(e.target.value)} className="w-full border px-2 py-1" />
              </div>
              <div>
                <label className="block text-sm mb-1">Time</label>
                <input type="time" value={time} onChange={e => setTime(e.target.value)} className="w-full border px-2 py-1" />
              </div>
              <div className="col-span-3 mt-1">
                <label className="block text-sm mb-1">AM/PM</label>
                <select value={ampm} onChange={e => setAmpm(e.target.value)} className="border px-2 py-1">
                  <option>AM</option>
                  <option>PM</option>
                </select>
              </div>
            </div>
            <div className="flex justify-end gap-2 mt-4">
              <button type="submit" className="px-3 py-1 bg-green-600 text-white rounded">Apply</button>
            </div>
          </form>
        </div>
      )}
    </div>
  )
}

function toLocalInput(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  const pad = (n) => String(n).padStart(2, '0')
  const date = `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`
  const time = `${pad(d.getHours())}:${pad(d.getMinutes())}`
  return `${date}T${time}`
}

function toLocalParts(iso) {
  // returns { date: 'YYYY-MM-DD', time: 'hh:mm', ampm: 'AM'|'PM' }
  if (!iso) return { date: '', time: '', ampm: 'AM' }
  const d = parseIsoDate(iso)
  if (!d) return { date: '', time: '', ampm: 'AM' }
  const pad = (n) => String(n).padStart(2, '0')
  const hours = d.getHours()
  const ampm = hours >= 12 ? 'PM' : 'AM'
  let hh = hours % 12
  if (hh === 0) hh = 12
  const time = `${pad(hh)}:${pad(d.getMinutes())}`
  const date = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
  return { date, time, ampm }
}

function parseIsoDate(iso) {
  if (!iso) return null
  // Some backends emit more than 3 fractional second digits; trim to milliseconds for JS Date parsing.
  const trimmed = iso.replace(/(\.\d{3})\d*(Z)?$/, '$1$2')
  const d = new Date(trimmed)
  if (Number.isNaN(d.getTime())) return null
  return d
}

function formatScheduled(iso) {
  const d = parseIsoDate(iso)
  return d ? d.toLocaleString() : 'No date'
}
