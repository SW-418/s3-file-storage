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
