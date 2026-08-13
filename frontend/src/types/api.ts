export type ApiResponse<T> = {
  success: boolean;
  message: string;
  data?: T;
  errorCode?: string;
};

export type ApiErrorBody = ApiResponse<never>;
