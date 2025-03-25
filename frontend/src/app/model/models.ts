export interface SearchObject {
    search: string
}

export interface TrackObject {
    spotifyId: string, 
    imageUrl: string, 
    trackName: string,
    artist: string[]
}

export interface CustomMarker { 
  position: google.maps.LatLngLiteral, 
  options: google.maps.MarkerOptions, 
  message: string,
  title: string,
  id: string, 
  song: TrackObject
}

export interface Playlist { 
  playlistId: string, 
  playlistName: string,
  playlistDescription: string,
  markers: CustomMarker[]
}