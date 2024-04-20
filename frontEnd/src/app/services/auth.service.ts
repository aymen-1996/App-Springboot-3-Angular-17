import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, catchError, map } from 'rxjs';
import { User } from '../models/user';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject!: BehaviorSubject<User>;
  public currentUser!: Observable<User>;

  constructor(private http: HttpClient) {

    this.currentUserSubject = new BehaviorSubject<User>(JSON.parse(localStorage.getItem('currentUser') as string));
    this.currentUser = this.currentUserSubject.asObservable();
   }

  public get currentUserValue(): User {
    return this.currentUserSubject.value;
  }

  login(email: string, password: string): Observable<any> {
    const body = { email, password };
  
    return this.http.post(`${environment.backendHost}/auth/authenticate`, body).pipe(
      map((user: any) => {
    
          localStorage.setItem('currentUser', JSON.stringify(user));

          this.currentUserSubject.next(user);
          return user; 
        
      }),
      catchError((error) => {
        console.error('Login error:', error);
        throw error;
      })
    );
  }
  
  getStoredUser(): User | null {
    return JSON.parse(localStorage.getItem('currentUser') as string);
  }
  
  getStoredUserId():any | null {
    const storedValue = localStorage.getItem('currentUser');
    if (storedValue) {
      const parsedValue = JSON.parse(storedValue);
      return { idUser: parsedValue.user.idUser };
    }
    return null;
  }
  
  getToken(): string {
    const storedUser = this.getStoredUser();
    return storedUser?.token ?? '';
  }
  checkToken(token: string): Observable<any> {
    return this.http.get<any>(`${environment.backendHost}/auth/check-token/${token}`);
  }

  logout() {
    localStorage.removeItem('currentUser');
  }


  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${environment.backendHost}/users/all`);
  }

  reorderUsers(users: User[]): Observable<void> {
    return this.http.put<void>(`${environment.backendHost}/users/reorder`, users);
  }

  deleteUser(userId: number): Observable<void> {
    const url = `${environment.backendHost}/users/${userId}`;
    return this.http.delete<void>(url);
  }

  requestPasswordReset(email: string): Observable<{ message: string }> {
    return this.http.post(`${environment.backendHost}/auth/request-code`, { email }, { responseType: 'text' })
        .pipe(
            map((response: any) => {
                return { message: response }; 
            })
        );
}



checkResetCode(email: string, resetCode: number): Observable<{ message: string }> {
  return this.http.post(`${environment.backendHost}/auth/check-reset-code`, { email, resetCode }, { responseType: 'text' })
      .pipe(
          map((response: any) => {
              return { message: response }; 
          })
      );
}

resetPassword(email: string, resetCode: number, newPassword: string, confirmPassword: string): Observable<{ message: string }> {
  return this.http.post(`${environment.backendHost}/auth/reset`, { email, resetCode, newPassword, confirmPassword }, { responseType: 'text' })
      .pipe(
          map((response: any) => {
              return { message: response }; 
          })
      );
}


register(registrationDto: User): Observable<any> {

  return this.http.post<any>(`${environment.backendHost}/auth/register`, registrationDto);
}

}
