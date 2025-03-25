import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Playlist } from '../model/models';
import { BehaviorSubject, catchError, Observable, tap, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PlaylistService {

  private httpClient = inject(HttpClient);

  private savePlaylistEndpoint = '/api/save/playlist'
  private getPlaylistEndpoint = '/api/get/playlist'
  private allPlaylistsEndpoint = '/api/playlists'
  private generateLyricsEndpoint = '/api/generate/lyrics'
  private generateSongsEndpoint = '/api/generate/songs'
  private getSongsEndpoint = '/api/get/songs'

  // send playlist to backend in map
  savePlaylist(username: string, playlist: Playlist ) {
    console.log(">>>Sending playlist to: ", this.savePlaylistEndpoint)
    console.log(">>>Username: ", username)
    console.log(">>>Playlist: ", playlist);

    // var toSend = JSON.stringify(playlist);
    return this.httpClient.post<Playlist>(
      `${this.savePlaylistEndpoint}`, 
      playlist,
      { params: { username } }
    )
  }

  // get playlist from backend 
  getPlaylist(username: string, playlistId: string): Observable<Playlist> {

    return this.httpClient.get<Playlist>(
      `${this.getPlaylistEndpoint}`, 
      { params: { username, playlistId } }
    )

  }

  getAllPlaylists(username: string) {

    return this.httpClient.get<Playlist[]>(
      `${this.allPlaylistsEndpoint}`,
      { params: { username } }
    )

  }

  generateLyrics(username: string, playlistId: string): Observable<string> {

    return this.httpClient.get(
      `${this.generateLyricsEndpoint}`, 
      { 
        params: { username, playlistId },
        responseType: 'text' // Specify that the response is plain text
      }
    )

  } 

  generateSong(username: string, playlistId: string, lyrics: string) {

    return this.httpClient.post(
      `${this.generateSongsEndpoint}`, 
      lyrics,
      {
        params: { username, playlistId }
      }
    )

  }

  getSongs(username: string, playlistId: string) {

    return this.httpClient.get(
      `${this.getSongsEndpoint}`, 
      {
        params: { username, playlistId }
      }
    )

  }

  // TESTING
  private playlistsSubject = new BehaviorSubject<Playlist[]>([]);
  playlists$ = this.playlistsSubject.asObservable();
  
  getPlaylists(username: string): Observable<Playlist[]> {
    console.log(">>>GET PLAYLISTS CALLED IN PLAYLIST SVC");
    return this.httpClient.get<Playlist[]>(`${this.allPlaylistsEndpoint}`, { params: { username } })
      .pipe(
        tap(playlists => {
          this.playlistsSubject.next(playlists);
        }),
        catchError(error => {
          console.error('Error fetching playlists:', error);
          return throwError(error);
        })
      );
  }
  
  refreshPlaylists(username: string): Observable<Playlist[]> {
    return this.getPlaylists(username).pipe(
      catchError(error => {
        console.error('Error refreshing playlists:', error);
        return throwError(error);
      })
    );
  }
}