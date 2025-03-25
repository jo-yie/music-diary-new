import { inject, Injectable } from "@angular/core";
import { Resolve } from "@angular/router";
import { Playlist } from "../model/models";
import { Observable } from "rxjs";
import { PlaylistService } from "../service/playlist.service";
import { SpotifyService } from "../service/spotify.service";

@Injectable({
  providedIn: 'root'
})
export class PlaylistsResolver implements Resolve<Playlist[]> {

  private playlistService = inject(PlaylistService);
  private spotifyService = inject(SpotifyService);

  resolve(): Observable<Playlist[]> {
    return this.playlistService
      .getAllPlaylists(this.spotifyService.getUserInfo())
  }
  
}