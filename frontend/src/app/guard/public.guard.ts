import { CanActivateFn, Router } from '@angular/router';
import { SpotifyService } from '../service/spotify.service';
import { inject } from '@angular/core';

export const publicGuard: CanActivateFn = (route, state) => {

  const spotifyService = inject(SpotifyService)
  const router = inject(Router)

  if (!spotifyService.isLoggedIn()) {
    return true
  }

  router.navigate(['/dashboard'])
  return false

};
