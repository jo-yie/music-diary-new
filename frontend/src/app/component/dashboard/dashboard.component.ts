import { Component } from '@angular/core';
import { SpotifyService } from '../../service/spotify.service';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { BehaviorSubject, filter, Observable, Subject, Subscription, takeUntil, tap } from 'rxjs';
import { Playlist } from '../../model/models';
import { PlaylistService } from '../../service/playlist.service';

@Component({
  selector: 'app-dashboard',
  standalone: false,
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent {
  user: any;
  recentlyPlayed: any = null;
  loading: boolean = false;
  error: string | null = null;

  isPlaylistSelected = false;
  isCreatePlaylist = false;

  playlists: Playlist[] = [];

  private routeSub: Subscription | undefined;

  constructor(
    private spotifyService: SpotifyService,
    private playlistService: PlaylistService,
    private router: Router, 
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    // Check if user is logged in
    this.user = this.spotifyService.getUserInfo();
    
    // Load recently played songs
    this.loadRecentlyPlayed();

    // Subscribe to route changes
    this.routeSub = this.router.events.pipe(
      filter(event => event instanceof NavigationEnd) // Only trigger on navigation
    ).subscribe(() => {
      this.checkRoute();
    });

    // Initial check when the component loads
    this.checkRoute();



    this.playlistService.playlists$
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: playlists => {
        this.playlists = playlists;
      },
      error: err => {
        console.error('Error receiving playlists:', err);
      }
    });

  // Initial load
  this.playlistService.refreshPlaylists(this.user).subscribe();

  }

  private destroy$ = new Subject<void>();


  ngOnDestroy(): void {
    if (this.routeSub) {
      this.routeSub.unsubscribe();
    }

    this.destroy$.next();
    this.destroy$.complete();

  }

  private checkRoute(): void {
    const childRoute = this.route.firstChild;
    
    if (childRoute) {
      const childPath = childRoute.snapshot.url[0]?.path;
      this.isPlaylistSelected = childPath === 'playlist' && !!childRoute.snapshot.params['id'];
      this.isCreatePlaylist = childPath === 'create-playlist';
    } else {
      this.isPlaylistSelected = false;
      this.isCreatePlaylist = false;
    }
  }

  close() {
    this.isPlaylistSelected = false;
    this.isCreatePlaylist = false;
  }

  loadRecentlyPlayed(): void {
    this.loading = true;
    this.error = null;
    
    this.spotifyService.getRecentlyPlayed(this.user).subscribe({
      next: (data) => {
        this.recentlyPlayed = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching recently played songs:', err);
        this.error = 'Failed to load your recently played songs';
        this.loading = false;
      }
    });

  }

  logout(): void { 
    this.spotifyService.logout()
    this.router.navigate(['/']);
  }

}
