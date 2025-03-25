import { ComponentStore } from '@ngrx/component-store'; 
import { Injectable } from '@angular/core';

// define the shape of the state 
interface PlayerState { 
    isPlaying: boolean; 
    currentTrack: any; 
    playerState: any;
}

@Injectable()
export class PlayerStore extends ComponentStore<PlayerState> {

    constructor() {
        // initialise the state 
        super({ isPlaying: false, currentTrack: null, playerState: null });
    }

    // updaters: functions to update state
    readonly setIsPlaying = 
        this.updater((state, isPlaying: boolean) => ({
            ...state,
            isPlaying
        }));

    readonly setCurrentTrack = 
        this.updater((state, currentTrack: any) => ({
            ...state, 
            currentTrack
        }));

    readonly setPlayerState = 
        this.updater((state, playerState: any) => ({
            ...state,
            playerState
        }));

    // selectors: functions to read state
    readonly isPlaying$ = this.select((state) => state.isPlaying);
    readonly currentTrack$ = this.select((state) => state.currentTrack);
    readonly playerState$ = this.select((state) => state.playerState);

}