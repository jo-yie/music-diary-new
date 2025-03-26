import { NgModule, isDevMode } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './component/login/login.component';
import { HttpClientModule } from '@angular/common/http';
import { AuthSuccessComponent } from './component/auth-success/auth-success.component';
import { AuthErrorComponent } from './component/auth-error/auth-error.component';
import { DashboardComponent } from './component/dashboard/dashboard.component';
import { SpotifyPlayerComponent } from './component/spotify-player/spotify-player.component';
import { AddSongComponent } from './component/add-song/add-song.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { GoogleMapsModule } from '@angular/google-maps';
import { MapComponent } from './component/map/map.component';
import { PlaylistComponent } from './component/playlist/playlist.component';
import { PlaylistsComponent } from './component/playlists/playlists.component';
import { GenerateSongComponent } from './component/generate-song/generate-song.component';
import { MaterialModule } from './module/material.module';
import { RouterModule } from '@angular/router';
import { ServiceWorkerModule } from '@angular/service-worker';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    AuthSuccessComponent,
    AuthErrorComponent,
    DashboardComponent,
    SpotifyPlayerComponent,
    AddSongComponent,
    MapComponent,
    PlaylistComponent,
    PlaylistsComponent,
    GenerateSongComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    GoogleMapsModule,
    FormsModule,
    MaterialModule,
    // ServiceWorkerModule.register('ngsw-worker.js', {
    //   // enabled: true,
    //   enabled: !isDevMode(),
    //   // Register the ServiceWorker as soon as the application is stable
    //   // or after 30 seconds (whichever comes first).
    //   registrationStrategy: 'registerWhenStable:30000'
    // })
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
