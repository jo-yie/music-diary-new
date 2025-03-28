import { Component, inject, OnInit, QueryList, ViewChild, ViewChildren, AfterViewInit, SimpleChanges } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PlaylistService } from '../../service/playlist.service';
import { SpotifyService } from '../../service/spotify.service';
import { CustomMarker, Playlist, TrackObject } from '../../model/models';
import { GoogleMap, MapInfoWindow, MapMarker } from '@angular/google-maps';

@Component({
  selector: 'app-playlist',
  standalone: false,
  templateUrl: './playlist.component.html',
  styleUrl: './playlist.component.css'
})
export class PlaylistComponent implements OnInit, AfterViewInit {
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private playlistService = inject(PlaylistService);
  private spotifyService = inject(SpotifyService);

  hasSongs = false;
  songUrls: any;
  songLyrics: string = '';
  songImageUrls: any;
  
  user!: string;
  deviceId!: string;
  playlistId: any;
  playlist!: Playlist;
  isMapReady: boolean = false;
  isMapBoundsFitted: boolean = false;

  currentView: string = 'all';
  
  @ViewChild(GoogleMap) map!: GoogleMap;
  @ViewChild(MapInfoWindow) infoWindow!: MapInfoWindow;
  @ViewChildren(MapMarker) mapMarkers!: QueryList<MapMarker>;
  
  selectedMarker: CustomMarker | null = null;
  options: google.maps.MapOptions = {
    mapId: "a0f03b17f7b725f4",
    zoomControl: true,
    scrollwheel: true,
    disableDoubleClickZoom: true,
    draggableCursor: 'pointer',
    // center: { lat: 1.3649170000000002, lng: 103.82287200000002 },
    // mapTypeId: "satellite",
    zoom: 10,
  };
  
  ngOnInit(): void {
    this.user = this.spotifyService.getUserInfo();
    this.deviceId = this.spotifyService.getDeviceId();

    this.route.paramMap.subscribe(params => {
      this.playlistId = params.get('id');
      this.loadPlaylist();
    });

    this.route.queryParamMap.subscribe(params => {
      this.currentView = params.get('view') || 'all';
    })

    this.playlistService.getSongs(this.user, this.playlistId)
      .subscribe({
        next: value => {
          this.songUrls = value;
          console.log(">>>Song Url Response: ", this.songUrls); 
          console.log(">>>this.songUrls: ", this.songUrls);

          // convert each audio URL to an image URL
          this.songImageUrls = this.songUrls.map((url: string) => 
            url.replace('/audio/', '/image/').replace('.m4a', '.jpg')
          );

          // check if songs is empty 
          if (this.songUrls.length === 0) {
            this.hasSongs = false; 

          } else { 
            this.hasSongs = true;

            this.playlistService.getSongLyrics(this.user, this.playlistId)
            .subscribe({
              next: value => {
                this.songLyrics = value
                console.log(">>>Lyrics: ", this.songLyrics)

              }, 
              error: err => {
                console.log(">>>Error: ", err);
              }
            })

          }
          console.log(">>>HAS SONGS: ", this.hasSongs);

        }, 
        error: err => {
          console.log(">>>Error: ", err);
        }
      }
    )

  }

  changeView(view: string) {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { view }, 
      queryParamsHandling: 'merge'
    })
  }
  
  ngAfterViewInit(): void {
    // Check if data was loaded before view initialized
    if (this.isMapReady && this.map && !this.isMapBoundsFitted) {
      this.setupMapWithBounds();
    }

  }
  
  loadPlaylist(): void {
    this.playlistService.getPlaylist(this.user, this.playlistId)
      .subscribe({
        next: value => {
          this.playlist = value;
          // Check if markers array exists and has items
          this.isMapReady = this.playlist &&
                           this.playlist.markers &&
                           this.playlist.markers.length > 0;
          
          console.log(">>>Playlist: ", this.playlist);
          
          // If the map view is already initialized, set up the map
          if (this.isMapReady && this.map && !this.isMapBoundsFitted) {
            this.setupMapWithBounds();
          }
        },
        error: err => {
          if (err.status === 404) {
            alert("Playlist not found!");
            this.router.navigate(['/dashboard']);
          }
          console.log(">>>Error: ", err);
        }
      });
  }
  
  onMapInitialized(): void {
    console.log(">>>Map initialized");
    if (this.isMapReady && !this.isMapBoundsFitted) {
      this.setupMapWithBounds();
    }
  }
  
  setupMapWithBounds(): void {
    // Hide the map initially (we'll handle display with isMapBoundsFitted flag)
    const mapElement = this.map.googleMap?.getDiv();
    if (mapElement) {
      mapElement.style.opacity = '0';
    }
    
    console.log(">>>Setting up map with bounds");
    if (!this.map || !this.playlist.markers || this.playlist.markers.length === 0) {
      console.log(">>>Map or markers not ready");
      return;
    }
    
    const bounds = new google.maps.LatLngBounds();
    
    // Add all marker positions to the bounds
    this.playlist.markers.forEach(marker => {
      bounds.extend(new google.maps.LatLng(
        marker.position.lat,
        marker.position.lng
      ));
    });
    
    // Only proceed if bounds has coordinates
    if (bounds.isEmpty()) {
      console.log(">>>Bounds is empty");
      return;
    }
    
    console.log(">>>Fitting bounds");
    
    // Special handling for single marker scenario
    if (this.playlist.markers.length === 1) {
      // For a single marker, don't use fitBounds as it zooms in too much
      const marker = this.playlist.markers[0];
      this.map.googleMap?.setCenter({
        lat: marker.position.lat,
        lng: marker.position.lng
      });
      // Set a reasonable zoom level for a single POI
      this.map.googleMap?.setZoom(12); // Adjust this value based on your preference
    } else {
      // For multiple markers, use fitBounds with padding
      this.map.fitBounds(bounds, 50);
    }
    
    // Wait for bounds to be applied, then show the map
    setTimeout(() => {
      this.isMapBoundsFitted = true;
      
      // Fade in the map
      if (mapElement) {
        mapElement.style.transition = 'opacity 0.3s';
        mapElement.style.opacity = '1';
      }
      
      console.log(">>>Map bounds fitted and map displayed");
    }, 300);
  }
  
  openInfoWindow(marker: CustomMarker): void {
    this.selectedMarker = marker;
    const mapMarkerArray = this.mapMarkers.toArray();
    const index = this.playlist.markers.findIndex(m =>
      m.position.lat === marker.position.lat &&
      m.position.lng === marker.position.lng
    );
    if (index !== -1 && mapMarkerArray[index]) {
      this.infoWindow.open(mapMarkerArray[index]);
    }

  }

  playSong(marker: CustomMarker) {

    console.log(">>>Play Song Clicked");
    this.openInfoWindow(marker);

    this.spotifyService.playSong(this.user, this.spotifyService.getDeviceId(), marker.song.spotifyId).subscribe({
      next: () => console.log('Play request successful'),
      error: err => console.error('Play request failed:', err)
    });
  }

  playSelectedSong() {

    console.log(">>>Play Selected Song Clicked");

    // TODO highlight the song playing somehow ??

    if (this.selectedMarker !== null) {

      this.spotifyService.playSong(this.user, this.spotifyService.getDeviceId(), this.selectedMarker.song.spotifyId).subscribe({
        next: () => console.log('Play request successful'),
        error: err => console.error('Play request failed:', err)
      });

    }

  }

  onMapClick(event: google.maps.MapMouseEvent): void {

    this.infoWindow.close();
    this.selectedMarker = null;

  }

}