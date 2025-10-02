terraform {
  backend "s3" {
    bucket  = "sw-418-tf-state"
    key     = "s3-file-storage"
    region  = "ca-central-1"
    profile = "s3-uploader"
  }
}
