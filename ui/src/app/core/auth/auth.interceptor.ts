import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const estRequeteApi = req.url.startsWith('/api') || req.url.startsWith('http://localhost:8080/api');
  const jeton = auth.autorisation;

  const requete =
    estRequeteApi && jeton && !req.headers.has('Authorization')
      ? req.clone({ setHeaders: { Authorization: jeton } })
      : req;

  return next(requete).pipe(
    catchError((err: unknown) => {
      if (err instanceof HttpErrorResponse && err.status === 401 && !req.url.endsWith('/api/me')) {
        auth.deconnecter();
        router.navigate(['/login']);
      }
      return throwError(() => err);
    }),
  );
};
