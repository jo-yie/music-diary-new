import { Component, OnInit } from '@angular/core';
import { SpotifyService } from '../../service/spotify.service';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {

  authUrl: string = ''
  isLoading: boolean = true 

  constructor(private spotifyService: SpotifyService) { }

  ngOnInit(): void {
      this.spotifyService.getAuthUrl().subscribe({
        next: (url) => {
          this.authUrl = url
          this.isLoading = false
        }, 
        error: (error) => {
          console.error('Error fetching auth url', error)
          this.isLoading = false
        }
      })
  }

  loginWithSpotify() {
    window.location.href = this.authUrl;
  }

}
