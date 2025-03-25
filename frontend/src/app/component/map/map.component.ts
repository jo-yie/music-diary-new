import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit, QueryList, ViewChild, ViewChildren } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { GoogleMap, MapInfoWindow, MapMarker } from '@angular/google-maps';
import { v4 as uuidv4 } from 'uuid';
import { CustomMarker, Playlist, TrackObject } from '../../model/models';
import { SpotifyService } from '../../service/spotify.service';
import { PlaylistService } from '../../service/playlist.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-map',
  standalone: false,
  templateUrl: './map.component.html',
  styleUrl: './map.component.css'
})

export class MapComponent implements OnInit {

  router = inject(Router);

  user!: string;
  spotifyService = inject(SpotifyService);

  playlistForm!: FormGroup; 
  playlistService = inject(PlaylistService);
  
  @ViewChild(GoogleMap) map!: GoogleMap; 
  @ViewChild(MapInfoWindow) infoWindow!: MapInfoWindow;

  fb = inject(FormBuilder);
  form!: FormGroup;

  markers: CustomMarker[] = [];
  selectedMarker: CustomMarker | null = null;
  selectedSong: TrackObject | null = null;

  newMarkerPosition: google.maps.LatLngLiteral | null = null;
  newMarkerMessage: string = '';
  newMarkerTitle: string = '';

  addingNewMarker: boolean = false; 

  options: google.maps.MapOptions = {
    mapId: "a0f03b17f7b725f4",
    // mapTypeId: 'roadmap',
    zoomControl: true, 
    scrollwheel: true, 
    disableDoubleClickZoom: true,
    draggableCursor: 'pointer',
    center: { lat: 1.290665504, lng: 103.772663576 }, // TODO set to singapore
    zoom: 12,
  };

  constructor(private httpClient: HttpClient) { }

  ngOnInit(): void {

    this.user = this.spotifyService.getUserInfo();

    this.getUserLocation();
    this.form = this.createForm();

    this.playlistForm = this.createPlaylistForm();

  }

  createForm(): FormGroup {
    return this.fb.group({
      title: this.fb.control<string>('', [ Validators.required ]), 
      message: this.fb.control<string>('')
    })
  }

  onSongSelected(song: TrackObject): void {
    this.selectedSong = song; 
    console.log(">>>Selected song in map component: ", song);
  }

  processForm(): void { 
    const values = this.form.value; 
    console.log(">>>Values:", values);

    if(!this.selectedSong) {
      console.log(">>>No song selected. Cannot create marker")
      return;
    }

    if (this.selectedMarker) {
      this.selectedMarker.title = values.title;
      this.selectedMarker.message = values.message;
      this.selectedMarker.song = this.selectedSong

      console.log(">>>selected marker:", this.selectedMarker);
      this.infoWindow.close();
      this.selectedMarker = null;
      this.selectedSong = null;

    } else if (this.newMarkerPosition) {

      // add a new marker
      const newMarker: CustomMarker = ({
        position: this.newMarkerPosition,
        options: { draggable: false },
        message: values.message,
        title: values.title,
        // id: this.markers.length 
        id: uuidv4().substring(0, 8),
        song: this.selectedSong
      });

      this.markers.push(newMarker);
      this.newMarkerPosition = null; // Reset after saving
      this.selectedSong = null;
    }

    this.addingNewMarker = false; // Close the form
    this.form = this.createForm(); // Reset the form

    console.log(">>>selected marker:", this.selectedMarker);
    console.log(">>>markers array:", this.markers);

  }

  getUserLocation(): void { 
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition((position) => {
        this.options.center = {
          lat: position.coords.latitude, 
          lng: position.coords.longitude
        };
      }, () => {
        // keep default centre
        console.log(">>>User denied location access or error occured")
      });
    }
  }

  onMapClick(event: google.maps.MapMouseEvent): void {

    this.infoWindow.close();
    this.selectedMarker = null;

    if (event.latLng) {
      this.newMarkerPosition = {
        lat: event.latLng.lat(),
        lng: event.latLng.lng()
      };
    }
    this.addingNewMarker = true;
    this.form = this.createForm();

  }

  cancelMarker(): void { 
    this.addingNewMarker = false;
    this.selectedSong = null;
    this.form = this.createForm();
  }

  editMarkerMessage(): void {
    this.addingNewMarker = true;
    this.selectedSong = null;
    this.form.setValue({
      title: this.selectedMarker!.title,
      message: this.selectedMarker!.message
    });

    // if marker has a song, set as selected osng 
    if (this.selectedMarker?.song) {
      this.selectedSong = this.selectedMarker.song;
    } else { 
      this.selectedSong = null; 
    }

  }

  @ViewChildren(MapMarker) mapMarkers!: QueryList<MapMarker>;

  openInfoWindow(marker: CustomMarker): void {

    // todo need to pass in marker?? or just can access this.selectedMarker directly 

    this.addingNewMarker = false;
    this.selectedMarker = marker;
  
    // Find the corresponding MapMarker component
    const mapMarkerArray = this.mapMarkers.toArray();
    const index = this.markers.findIndex(m => 
      m.position.lat === marker.position.lat && 
      m.position.lng === marker.position.lng
    );
  
    if (index !== -1 && mapMarkerArray[index]) {
      this.infoWindow.open(mapMarkerArray[index]);
    }
  }

  deleteMarker(): void {
    if (this.selectedMarker) {
      const markerToDelete = this.selectedMarker;
  
      // remove marker from the markers array
      this.markers = this.markers.filter(m => m.id !== markerToDelete.id);
  
      // close the info window
      // close form if open 
      this.infoWindow.close();
      this.addingNewMarker = false;
      this.selectedMarker = null;
    }
  }
  
  isFormValid(): boolean {
    return this.form.valid && this.selectedSong !== null;
  }

  createPlaylistForm(): FormGroup {
    return this.fb.group({
      playlistName: this.fb.control<string>("", [ Validators.required ]),
      playlistDescription: this.fb.control<string>("", [ Validators.required ])
    })
  }

  processPlaylistForm(): void { 

    const values = this.playlistForm.value; 
    
    const newPlaylist: Playlist = ({
      playlistId: uuidv4().substring(0, 8),
      playlistName: values.playlistName,
      playlistDescription: values.playlistDescription,
      markers: this.markers
    })

    console.log(">>>Playlist Object: ", newPlaylist);

    // TODO send to backend 
    this.playlistService.savePlaylist(this.user, newPlaylist).subscribe({
      next: (response) => {
        console.log(">>>Response: ", response)
          // Redirect to dashboard after short delay
          // setTimeout(() => {
            this.playlistService.refreshPlaylists(this.user).subscribe(() => {
              this.router.navigate(['/dashboard/playlist', newPlaylist.playlistId]);
            });
            // this.router.navigate(['/dashboard/playlist', newPlaylist.playlistId]);
          // }, 3000);


          this.playlistService.refreshPlaylists(this.user).subscribe({
            next: () => {
              this.router.navigate(['/dashboard/playlist', newPlaylist.playlistId]);
            },
            error: (err) => {
              console.error('Error refreshing after creation:', err);
              // You might still want to navigate even if refresh fails
              this.router.navigate(['/dashboard/playlist', newPlaylist.playlistId]);
            }
          });

      }, 
      error: (err) => {
        console.log(">>>Error: ", err)
      }
    });

    console.log(">>>Sent to backend");
    console.log(">>>ID: ", newPlaylist.playlistId);

  }

  isPlaylistValid(): boolean {
    return this.playlistForm.valid && this.markers.length > 0;
  }

  clearSelectedSong() {

  }

}