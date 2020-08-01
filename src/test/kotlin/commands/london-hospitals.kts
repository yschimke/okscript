#!/usr/bin/env kotlin

import com.baulsupp.okurl.kotlin.*
import com.baulsupp.okurl.location.Location
import com.baulsupp.okurl.services.mapbox.staticMap

data class Hospital(
  val OrganisationName: String,
  val Latitude: Double,
  val Longitude: Double,
  val Website: String?,
  val Phone: String?,
  val Email: String?
) {
  val location = Location(Latitude, Longitude)
}
data class HospitalResultSet(val rows: List<Hospital>)

val hospitals = query<HospitalResultSet>("https://nhs-england-hospitals.now.sh/hospitals-0cda400/hospitals.jsono?City=${"London"}").rows

show(staticMap {
  pinLocation(location())
  pinLocations(hospitals.take(50).map { it.location }, "s-hospital")
})
