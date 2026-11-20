import { HttpClient, HttpHeaders } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

export interface Utilisateur {
  readonly login: string;
  readonly roles: readonly string[];
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private jetonBasic: string | null = null;

  readonly utilisateur = signal<Utilisateur | null>(null);

  get autorisation(): string | null {
    return this.jetonBasic;
  }

  async connecter(login: string, motDePasse: string): Promise<void> {
    const header = 'Basic ' + btoa(`${login}:${motDePasse}`);
    const headers = new HttpHeaders({ Authorization: header });

    const profil = await firstValueFrom(
      this.http.get<Utilisateur>('http://localhost:8080/api/me', { headers }),
    );

    this.jetonBasic = header;
    this.utilisateur.set(profil);
  }

  deconnecter(): void {
    this.jetonBasic = null;
    this.utilisateur.set(null);
  }
}
