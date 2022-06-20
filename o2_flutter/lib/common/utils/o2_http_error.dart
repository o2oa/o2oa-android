

class O2HttpError extends Error {
  final String message;
  O2HttpError({required this.message});
}

class O2ValidateError extends Error {
  final message;
  O2ValidateError(this.message);
}