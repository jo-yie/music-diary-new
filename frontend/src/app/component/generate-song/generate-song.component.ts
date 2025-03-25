import { Component, inject, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PlaylistService } from '../../service/playlist.service';
import { SpotifyService } from '../../service/spotify.service';
import { CustomMarker, Playlist, TrackObject } from '../../model/models';
import { Router } from '@angular/router';

@Component({
  selector: 'app-generate-song',
  standalone: false,
  templateUrl: './generate-song.component.html',
  styleUrl: './generate-song.component.css'
})
export class GenerateSongComponent implements OnInit {

  @Input() playlist!: Playlist; 

  router = inject(Router);
  private fb = inject(FormBuilder); 
  protected form!: FormGroup; 

  user!: any;

  lyricsLoading = false;
  lyricsLoaded = false;
  lyrics: any;
  addedSongs: TrackObject[] = [];

  songLoading = false;
  songLoaded = false;
  songUrls: any;

  private spotifyService = inject(SpotifyService);
  private playlistService = inject(PlaylistService);

  ngOnInit(): void {

    this.user = this.spotifyService.getUserInfo();
    this.form = this.createForm();

  }

  createForm(): FormGroup {

    return this.fb.group({
      lyricsValue: this.fb.control<string>(this.lyrics, [ Validators.required ]),
    })

  }

  generateLyrics() { 

    console.log(">>>Generate Lyrics Button Clicked");
    this.lyricsLoading = true;

    this.playlistService.generateLyrics(this.user, this.playlist.playlistId)
    .subscribe({
      next: value => {
        this.lyrics = value; 
        this.lyricsLoaded = true; // Mark lyrics as loaded
        this.lyricsLoading = false;
        this.form.patchValue({ lyricsValue: this.lyrics }); // Update textarea
        console.log(">>>Lyrics: ", this.lyrics);
      }, 
      error: err => { 
        this.lyricsLoading = false;
        console.log(">>>Error: ", err)
      }
    })

  }

  processForm() { 

    const values = this.form.value; 
    console.log(">>>Values: ", values);

    this.songLoading = true;

    this.playlistService.generateSong(
      this.user, 
      this.playlist.playlistId,
      values.lyricsValue)
      .subscribe({
        next: value => {
          console.log(">>>Values: ", value);
          this.songUrls = value;
          this.songLoaded = true;
          window.location.reload()
          this.songLoading = false;
          console.log(">>>Song Urls: ", this.songUrls);
        }, 
        error: err => {
          this.songLoading = false;
          console.log(">>>Error: ", err);
        }
      })

  }

}