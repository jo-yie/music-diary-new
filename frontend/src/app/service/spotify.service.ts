import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Playlist } from '../model/models';

@Injectable({
  providedIn: 'root'
})
export class SpotifyService {

  // private baseUrl = 'http://localhost:8080';
  private authUrlEndpoint = '/api/spotify/auth/url';
  private callbackEndpoint = '/api/callback';
  private recentlyPlayedEndpoint = '/api/recently-played';
  private accessTokenEndpoint = '/api/access-token';
  private searchSongEndpoint = '/api/search';

  constructor(private httpClient: HttpClient) { }

  getAuthUrl(): Observable<any> { 
    return this.httpClient.get(`${this.authUrlEndpoint}`, 
      { responseType: 'text' }
    )
  }

  processCallback(code: string): Observable<any> {
    return this.httpClient.get(`${this.callbackEndpoint}?code=${code}`)
  }

  // TODO username can come from getUserInfo NOT from input
  getRecentlyPlayed(username: string): Observable<any> {
    return this.httpClient.get(`${this.recentlyPlayedEndpoint}`,
      { params: {username} } 
    )
  }

  getAccessToken(username: string): Observable<any> {
    return this.httpClient.get(`${this.accessTokenEndpoint}`,
      { params: {username}, responseType: 'text' }
    )
  }

  transferPlayback(deviceId: string, accessToken: string): Observable<any> {
    return this.httpClient.put(
      'https://api.spotify.com/v1/me/player',
      { device_ids: [deviceId], play: true },
      { 
        headers: { 
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        }
      }
    );
  }

  // store user info in local storage
  storeUserInfo(user: any): void { 
    localStorage.setItem('user', JSON.stringify(user))
  }

  // get user info from local storage 
  getUserInfo(): any { 
    const userStr = localStorage.getItem('user')
    return userStr ? JSON.parse(userStr) : null
  }

  // check if user is logged in 
  isLoggedIn(): boolean { 
    return !!this.getUserInfo()
  }

  // logout 
  logout(): void {
    localStorage.removeItem('user')
  }

  // 

  // search in add-song
  searchForSong(username: string, search: string): Observable<any> {
    return this.httpClient.get(`${this.searchSongEndpoint}`, 
      { params: { username, search } }
    );

  }

}