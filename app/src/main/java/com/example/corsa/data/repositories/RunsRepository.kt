package com.example.corsa.data.repositories

import com.example.corsa.data.model.Run
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.ZonedDateTime

// ── Interface (what the ViewModel depends on) ──────────────────────────────
interface RunsRepository {
    fun getRunById(id: String): Flow<Run?>
    fun getRunsByUser(userId: String): Flow<List<Run>>
}

// ── Fake implementation (no real Supabase yet) ─────────────────────────────
class FakeRunsRepository : RunsRepository {

    // Realistic GeoJSON path around a park loop
    private val fakePath = """
    {
      "type": "FeatureCollection",
      "features": [
        {
          "type": "Feature",
          "properties": {},
          "geometry": {
            "type": "LineString",
            "coordinates": [
              [12.003215, 44.2160733],
              [9.1875, 45.4670],
              [9.1901, 45.4682],
              [9.1923, 45.4665],
              [9.1910, 45.4648],
              [9.1880, 45.4638],
              [9.1859, 45.4654]
            ]
          }
        }
      ]
    }
""".trimIndent()

    private val fakeRuns = listOf<Run>()

    override fun getRunById(id: String): Flow<Run?> = flow {
        emit(fakeRuns.firstOrNull { it.id == id })
    }

    override fun getRunsByUser(userId: String): Flow<List<Run>> = flow {
        emit(fakeRuns.filter { it.userId == userId })
    }
}