resource "aws_s3_bucket" "s3-uploader-storage" {
  bucket = var.uploader_s3_bucket_name
}

resource "aws_s3_bucket_lifecycle_configuration" "s3-uploader-storage-lifecycle-config" {
  bucket = var.uploader_s3_bucket_name

  rule {
    id     = "abort-incomplete-uploads"
    status = "Enabled"

    // Automatically cleans up hanging multipart uploads after a day to save that $$$
    abort_incomplete_multipart_upload {
      days_after_initiation = 1
    }
  }
}

resource "aws_s3_bucket_cors_configuration" "s3-uploader-storage-cors-config" {
  bucket = var.uploader_s3_bucket_name

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["PUT"]
    allowed_origins = ["http://localhost:8080"]
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }
}
