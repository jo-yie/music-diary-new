import { Component, OnDestroy, OnInit, Renderer2 } from '@angular/core';
import { SpotifyService } from '../../service/spotify.service';
import { PlayerStore } from '../../store/player.store';
import { take } from 'rxjs';

// extend window interface to include Spotify types
declare global { 
  interface Window { 
    onSpotifyWebPlaybackSDKReady: () => void; 
    Spotify: any;
  }
}

@Component({
  selector: 'app-spotify-player',
  standalone: false,
  templateUrl: './spotify-player.component.html',
  styleUrl: './spotify-player.component.css', 
  providers: [ PlayerStore ]
})

export class SpotifyPlayerComponent implements OnInit, OnDestroy {
  
  player: any; 
  deviceId: string = ''; 
  accessToken: string = '';
  user: string = '';

  constructor(private spotifyService: SpotifyService,
    public playerStore: PlayerStore
  ) { }
  
  ngOnInit(): void {

    // load Spotify Web Playback SDK 
    const script = document.createElement('script'); 
    script.src = 'https://sdk.scdn.co/spotify-player.js';
    script.async = true; 
    document.body.appendChild(script);

    this.retrieveUserInfo();
    this.initializePlayer();

  }

  ngOnDestroy(): void {

    // disconnect the player when component is destroyed 
    if (this.player) {
      this.player.disconnect();
    }

  }

  // get access token and user 
  retrieveUserInfo() { 
    this.user = this.spotifyService.getUserInfo(); 
    this.spotifyService.getAccessToken(this.user)
      .subscribe({
        next: token => {
          this.accessToken = token; 
          console.log(">>>Access Token: ", this.accessToken);
        }, 
        error: err => console.error(">>>Error: ", err)
      });
  }

  initializePlayer() { 

    window.onSpotifyWebPlaybackSDKReady = () => {
      this.player = new window.Spotify.Player({
        name: 'My Angular Spotify Player', 
        getOAuthToken: (cb: (token: string) => void) => {
          cb(this.accessToken)
        },
        volume: 0.5
      })

      // error handler
      this.player.addListener('initialization_error', ({ message }: any) => {
        console.error('Failed to initialize player:', message);
      });
      this.player.addListener('authentication_error', ({ message }: any) => {
        console.error('Failed to authenticate:', message);
      });
      this.player.addListener('account_error', ({ message }: any) => {
        console.error('Failed to validate account:', message);
      });
      this.player.addListener('playback_error', ({ message }: any) => {
        console.error('Failed to perform playback:', message);
      });

      // playback status updates 
      this.player.addListener('player_state_changed', (state: any) => {
        this.playerStore.setPlayerState(state);
        this.playerStore.setIsPlaying(!state.paused);

        if (state.track_window.current_track) {
          this.playerStore.setCurrentTrack(state.track_window.current_track);
        }

      });

      // ready 
      this.player.addListener('ready', ({ device_id }: any) => {
        console.log(">>>Ready with Device ID", device_id);
        this.deviceId = device_id;

        // save to local storage 
        localStorage.setItem('deviceId', JSON.stringify(this.deviceId));

        // transfer playback from device 
        this.transferPlaybackToDevice();

      })

      // not ready 
      this.player.addListener('not_ready', ({ device_id}: any) => {
        console.log(">>>Device ID has gone offline", device_id);
      });

      // connect to player 
      this.player.connect();

    }

  }

  transferPlaybackToDevice() {
    if (this.deviceId && this.accessToken) {
      this.spotifyService.transferPlayback(this.deviceId, this.accessToken)
        .subscribe({
          next: () => {
            console.log(">>>Playback transferred successfully"); 
            this.startPlayerStatePolling();
          },
          error: (error) => console.error(">>>Error transferring playback:", error)
    });
    } else { 
      console.error('>>>Cannot transfer playback, deviceId or token is missing')
    }
  }

  // poll for player state updates 
  startPlayerStatePolling() { 
    const pollingInterval = setInterval(() => {
      if (this.player) {
        this.player.getCurrentState().then((state: any) => {
          if (state) {
            this.playerStore.setPlayerState(state); 
            this.playerStore.setIsPlaying(!state.paused);

            if (state.track_window.current_track) {
              this.playerStore.setCurrentTrack(state.track_window.current_track);

              clearInterval(pollingInterval);
            }
          }
        });
      }
    }, 1000);
    
    setTimeout(() => {
      clearInterval(pollingInterval);
    }, 10000);
  }

  togglePlay() { 
    if (this.player) {
      this.player.togglePlay().then(() => {
        this.playerStore.state$.pipe(take(1)).subscribe((isPlaying) => {
          this.playerStore.setIsPlaying(!isPlaying);
        })
      });
    }
  }

  previousTrack() { 
    if (this.player) {
      this.player.previousTrack().then(() => {
        setTimeout(() => {
          this.refreshPlayerState();
        }, 1000);
      });
    }
  }

  nextTrack() { 
    if (this.player) {
      this.player.nextTrack().then(() => {
        setTimeout(() => {
          this.refreshPlayerState();
        }, 1000);
      });
    }
  }

  refreshPlayerState() {
    if (this.player) {
      this.player.getCurrentState().then((state: any) => {
        if (state) {
          this.playerStore.setPlayerState(state);
          this.playerStore.setIsPlaying(!state.paused);
          if (state.track_window?.current_track) {
            this.playerStore.setCurrentTrack(state.track_window.current_track);
          }
        }
      });
    }
  }

  
}
