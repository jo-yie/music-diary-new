import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SpotifyService } from '../service/spotify.service';

export const authGuard: CanActivateFn = (route, state) => {

  const spotifyService = inject(SpotifyService)
  const router = inject(Router)

  if (spotifyService.isLoggedIn()) {
    return true
  }

  router.navigate(['/'])
  return false

};
