import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SpotifyService } from '../../service/spotify.service';

@Component({
  selector: 'app-auth-success',
  standalone: false,
  templateUrl: './auth-success.component.html',
  styleUrl: './auth-success.component.css'
})
export class AuthSuccessComponent implements OnInit {

  loading: boolean = true
  error: string | null = null

  constructor(private route: ActivatedRoute, private router: Router, private spotifyService: SpotifyService) { }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const encodedData = params['data'];
      
      if (!encodedData) {
        this.error = 'No user data received';
        this.loading = false;
        return;
      }
      
      try {
        // Decode the base64 encoded user data
        // const jsonStr = atob(encodedData);
        // const userData = JSON.parse(jsonStr);

        console.log(">>>User Data: ", encodedData)
        
        // Store user info in local storage
        this.spotifyService.storeUserInfo(encodedData);

        console.log(">>>User Data saved in local storage")
        
        // Redirect to dashboard after short delay
        setTimeout(() => {
          this.router.navigate(['/dashboard']);
        }, 3000);
        
      } catch (e) {
        console.error('Error processing user data:', e);
        this.error = 'Failed to process authentication data';
        this.loading = false;
      }
    });
  }

}
