import { Component, inject, OnInit } from '@angular/core';
import { PlaylistService } from '../../service/playlist.service';
import { SpotifyService } from '../../service/spotify.service';
import { Playlist } from '../../model/models';

@Component({
  selector: 'app-playlists',
  standalone: false,
  templateUrl: './playlists.component.html',
  styleUrl: './playlists.component.css'
})
export class PlaylistsComponent implements OnInit {

  private playlistService = inject(PlaylistService);
  private spotifyService = inject(SpotifyService);

  user!: string;
  playlists: Playlist[] = [];

  ngOnInit(): void {

    this.user = this.spotifyService.getUserInfo();
    this.playlistService.getAllPlaylists(this.user)
      .subscribe({
        next: value => {
          this.playlists = value
          console.log(">>>Playlists: ", this.playlists)
        }, 
        error: err => { 
          console.log(">>>Error: ", err)
        }
      })

  }

}