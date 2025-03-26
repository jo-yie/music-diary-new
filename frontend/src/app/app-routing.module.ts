import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './component/login/login.component';
import { AuthSuccessComponent } from './component/auth-success/auth-success.component';
import { AuthErrorComponent } from './component/auth-error/auth-error.component';
import { DashboardComponent } from './component/dashboard/dashboard.component';
import { publicGuard } from './guard/public.guard';
import { authGuard } from './guard/auth.guard';
import { SpotifyPlayerComponent } from './component/spotify-player/spotify-player.component';
import { AddSongComponent } from './component/add-song/add-song.component';
import { MapComponent } from './component/map/map.component';
import { PlaylistComponent } from './component/playlist/playlist.component';
import { PlaylistsComponent } from './component/playlists/playlists.component';
import { GenerateSongComponent } from './component/generate-song/generate-song.component';
import { platformBrowser } from '@angular/platform-browser';
import { PlaylistsResolver } from './resolver/playlists.resolver';

const routes: Routes = [
  { 
    path: '', 
    component: LoginComponent,
    canActivate: [publicGuard]
  }, 
  { path: 'auth-success', component: AuthSuccessComponent }, 
  { path: 'auth-error', component: AuthErrorComponent }, 
  { 
    path: 'dashboard', 
    component: DashboardComponent, // router outlet is here
    resolve: { playlists: PlaylistsResolver },
    canActivate: [authGuard],
    children: [
      // {
      //   path: 'playlists', // child route path
      //   component: PlaylistsComponent
      // },
      {
        path: 'playlist/:id', 
        component: PlaylistComponent
      }, 
      {
        path: 'create-playlist', 
        component: MapComponent, 
        canActivate: [authGuard]
      }
    ]
  },
  { 
    path: 'player', 
    component: SpotifyPlayerComponent, 
    canActivate: [authGuard]
  },
  { 
    path: 'add-song', 
    component: AddSongComponent, 
    canActivate: [authGuard]
  },
  // {
  //   path: 'map', 
  //   component: MapComponent, 
  //   canActivate: [authGuard]
  // },
  // {
  //   path: 'playlists', 
  //   component: PlaylistsComponent, 
  //   canActivate: [authGuard]
  // },
  // {
  //   path: 'playlist/:id', 
  //   component: PlaylistComponent, 
  //   canActivate: [authGuard]
  // },
  // temporary: 
  {
    path: 'generate-song',
    component: GenerateSongComponent, 
    canActivate: [authGuard]
  },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
