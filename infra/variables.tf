variable "aws_region" {
  type = string
  default = "ca-central-1"
  description = "AWS region for resources"
}

variable "aws_profile" {
  type = string
  default = "s3-uploader"
  description = "AWS CLI profile to use"
}

variable "uploader_s3_bucket_name" {
  type = string
  default = "s3-uploader-storage"
  description = "Default bucket used for this project (separate to tf-state bucket)"
}
