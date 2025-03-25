import { Component, EventEmitter, inject, OnInit, Output } from '@angular/core';
import { SpotifyService } from '../../service/spotify.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SearchObject, TrackObject } from '../../model/models';

@Component({
  selector: 'app-add-song',
  standalone: false,
  templateUrl: './add-song.component.html',
  styleUrl: './add-song.component.css'
})
export class AddSongComponent implements OnInit {

  @Output() songSelected = new EventEmitter<TrackObject>();

  private spotifyService = inject(SpotifyService)
  private fb = inject(FormBuilder);
  protected form!: FormGroup; 

  user!: string;
  searchResult: any;
  added: any;
  addedSongs: TrackObject[] = [];

  ngOnInit(): void {
    this.user = this.spotifyService.getUserInfo();
    console.log(">>>User set", this.user);

    this.form = this.createForm(); 
    console.log(">>>Form created");

  }

  createForm(): FormGroup { 
    return this.fb.group({
      search: this.fb.control<string>("", [ Validators.required ])
    })
  }

  processForm(): void { 

    // const values = this.form.value;
    const values: SearchObject = this.form.value
    console.log(">>>Values", values);

    this.spotifyService.searchForSong(this.user, values.search).subscribe({
      next: (data) => {
        console.log(">>>Data:", data);
        this.searchResult = data;
        console.log(">>>Updated searchResult:", this.searchResult);
      }, 
      error: (err) => {
        console.error(">>>Error:", err);
      }
    })

  }

  addSong(track: any): void { 
    console.log(">>>Add song pressed");

    const newTrack: TrackObject = {
      spotifyId: track.id, 
      imageUrl: track.album.images[0].url, 
      trackName: track.name, 
      artist: track.artists.map((a: any) => a.name)
    };

    // clear previous selection and add new song 
    this.addedSongs = [newTrack];

    // emit selected song to parent component 
    this.songSelected.emit(newTrack);

    // // check if song is already added
    // if (!this.addedSongs.some(song => song.spotifyId === newTrack.spotifyId)) {
    //   this.addedSongs.push(newTrack);
    // }

  }
  
  removeSong(track: any): void {
    console.log(">>>Remove song pressed");
    this.addedSongs = this.addedSongs.filter(song => song.spotifyId !== track.spotifyId);

    // if all songs removed, emit null to clear selection 
    if (this.addedSongs.length === 0) {
      this.songSelected.emit(null as any)
    }

  }

}