
var requestWithGET = function(url) {
	return fetch(url)
		.then((response) => {
			if (!response.ok) {
				throw new Error('Received non-200 HTTP status code: ' + response.status);
			} else {
				return response.blob();
			}
		})
}

var displayDoomAssets = function(responseToRequest, elapsedTime, labelForDoomAssets) {
	var wadImages = responseToRequest.wadImages
	var parentOfImages = document.getElementById('resultsGoHere');
	while (parentOfImages.lastChild) {
		parentOfImages.removeChild(parentOfImages.lastChild);
	}
	
	if (wadImages === undefined) {	// error occurred
		console.log(responseToRequest);
		var errorContainer = document.createElement("div");
		errorContainer.className = "errorContainer";
		errorContainer.innerHTML = "An error was encountered while processing the data you submitted! Perhaps the data wasn't a WAD file intended for Doom 1?";
		parentOfImages.appendChild(errorContainer)
	} else {
		var imgElementForNamedImage = function(namedImage) {
			var imgElement = document.createElement("img");
			imgElement.src = "data:image/png;base64," + namedImage.imageAsBase64EncodedPng;
			imgElement.title = namedImage.name;
			imgElement.className = "imageFromWAD";
			return imgElement
		}
		var processSetOfImages = function(title, description, images, parentOfImages) {
			if (images.length > 0 ) {
				var container = document.createElement("div");
				container.className = "sectionContainer";
				
				var headerDivElement = document.createElement("div");
				headerDivElement.className = "header";
				container.appendChild(headerDivElement);
			
				var detailsSpanElement = document.createElement("span");
				detailsSpanElement.className = "details";
				headerDivElement.appendChild(detailsSpanElement);
				var titleSpanElement = document.createElement("span");
				titleSpanElement.className = "title";
				detailsSpanElement.appendChild(titleSpanElement);
				var descriptionSpanElement = document.createElement("span");
				descriptionSpanElement.className = "description";
				detailsSpanElement.appendChild(descriptionSpanElement);
				
				titleSpanElement.innerHTML = title;
				descriptionSpanElement.innerHTML = description;
				
				for (var i = 0; i < images.length; i++) {
					container.appendChild(imgElementForNamedImage(images[i]));
				}
				parentOfImages.appendChild(container)
				return true;
			}
			else {
				return false;
			}
		}

		var resultHeader = document.createElement("div");
		resultHeader.className = "resultHeader";
		
		var fileNameSpanElement = document.createElement("span");
		fileNameSpanElement.className = "fileName";
		fileNameSpanElement.innerHTML = labelForDoomAssets;
		resultHeader.appendChild(fileNameSpanElement);
		
		parentOfImages.appendChild(resultHeader)
		
		var spritesProcessed = processSetOfImages("sprites", "objects that appear inside a map", wadImages.sprites, parentOfImages);
		var flatsProcessed = processSetOfImages("flats", "used on ceilings and floors", wadImages.flats, parentOfImages);
		var texturesProcessed = processSetOfImages("textures", "used on walls", wadImages.textures, parentOfImages);
		var otherGraphicsProcessed = processSetOfImages("other graphics", "UI elements and miscellaneous other images", wadImages.otherGraphics, parentOfImages);
	
		if (!(flatsProcessed || spritesProcessed || texturesProcessed || otherGraphicsProcessed)) {
			// let the user know that no images were found, good chance this WAD just introduces new maps using all the original image assets from doom
			var errorContainer = document.createElement("div");
			errorContainer.className = "noImagesContainer";
			errorContainer.innerHTML = "No images were found while processing the WAD you submitted.  This is unfortunate, but not unexpected because many Doom WADs only introduce new maps using all the original image/texture assets from Doom.";
			parentOfImages.appendChild(errorContainer)
		}
	}

	var createTimingDiv = function(description, time_in_ms) {
		var timingInformation = document.createElement("div");
		timingInformation.innerHTML = description + ": " + (time_in_ms / 1000) + " seconds";
		return timingInformation;
	}

	var createTimingListItem = function(description, time_in_ms) {
		var listItem = document.createElement("li");
		listItem.appendChild(createTimingDiv(description, time_in_ms));
		return listItem;
	}

	var statsSection = document.createElement("div");
	statsSection.id = "stats";
	parentOfImages.appendChild(statsSection);
	var statsLabel = document.createElement("div");
	statsLabel.id = "label";
	statsLabel.innerHTML = "Stats";
	var statsContents = document.createElement("div");
	statsContents.id = "contents";
	statsSection.appendChild(statsLabel);
	statsSection.appendChild(statsContents);
	statsContents.appendChild(createTimingDiv("Total time seen by browser", elapsedTime));
	if (typeof responseToRequest.timings !== 'undefined') {
		var subtimingsList = document.createElement("ul");
		statsContents.appendChild(subtimingsList);
		subtimingsList.appendChild(createTimingListItem("time spent parsing file", responseToRequest.timings.timeToParseFile_in_ms));
		subtimingsList.appendChild(createTimingListItem("time spent building images", responseToRequest.timings.timeToBuildImages_in_ms));
	}
}

var uriIsBeingDropped = function(jqueryDndEvent) {
	var hasUriList = -1 !== $.inArray("text/uri-list", jqueryDndEvent.originalEvent.dataTransfer.types);
	var hasUrl = -1 !== $.inArray("Url", jqueryDndEvent.originalEvent.dataTransfer.types);	// for IE: this was the value that I was seeing in IE 11
	var hasURL = -1 !== $.inArray("URL", jqueryDndEvent.originalEvent.dataTransfer.types);	// for IE: http://stackoverflow.com/a/18051912
	// sadly, it looks like (at least with IE 11) we can't just call event.originalEvent.dataTransfer.getData("URL"/"Url") here and check for a non-empty response, because that call is returning "" in onDragEnter
	return hasUriList || hasUrl || hasURL;
}

var fileIsBeingDropped = function(jqueryDndEvent) {
	var hasFiles = -1 !== $.inArray("Files", jqueryDndEvent.originalEvent.dataTransfer.types);
	return hasFiles;
}

var onDragEnter = function(event) {
	// understanding drag and drop for html is not easy....doesn't seem to be a clear way to communicate 'these are the allowed actions' and have that reflect with what the user's trying to do
	if ((uriIsBeingDropped(event) || fileIsBeingDropped(event)) && !$(event.currentTarget).hasClass("dropIsBeingProcessed")) {
		$(event.currentTarget).addClass("dropIsValid")
		event.originalEvent.dataTransfer.dropEffect = "copy"
		event.preventDefault();
	}
}

var onDragOver = function(event) {
	if ((uriIsBeingDropped(event) || fileIsBeingDropped(event)) && !$(event.currentTarget).hasClass("dropIsBeingProcessed")/* dragOver can happen even if dragEnter is ignored :( */) {
		event.originalEvent.dataTransfer.dropEffect = "copy"
		event.preventDefault();
	}
}

var onDragLeave = function(event) {
	$(event.currentTarget).removeClass("dropIsValid")
	event.preventDefault();
}

var extractLabelFromAnchorHtml = function(anchorHtml) {
	var tempDiv = document.createElement("div");
	tempDiv.innerHTML = anchorHtml;
	var anchors = tempDiv.getElementsByTagName("A");
	if (anchors.length == 1)	// there should be only anchor child
	{
		var anchorElement = anchors[0];
		if (anchorElement.children.length == 0)	// innerHTML is all text?
		{
			return anchorElement.innerHTML;
		}
	}
	
	return "";
}

var onDrop = function(loadingImage, parseWadData) {
	return function(event) {
		// create a function for extracting some user-facing label to display for the loading process
		var getLabelFromDndEvent = function(event) {
			if (uriIsBeingDropped(event)) {	// links
				var uri = event.originalEvent.dataTransfer.getData("URL");	// this retrieves the first URL out of a possible list (https://developer.mozilla.org/en-US/docs/Web/Guide/HTML/Recommended_Drag_Types)
				
				var hasAnchorHTML = -1 !== $.inArray("text/html", event.originalEvent.dataTransfer.types);
				var uriLabel = hasAnchorHTML ? extractLabelFromAnchorHtml(event.originalEvent.dataTransfer.getData("text/html")) : uri;
				return uriLabel;
			} else if (fileIsBeingDropped(event)) {	// files on disk
								
				var files = event.originalEvent.dataTransfer.files; // Array of all files
				return files[0].name;
			}
		};

		var possiblyUnzipAndConverToArraybuffer = function(dataBlob) {
			return JSZip.loadAsync(dataBlob).then(
				function (zip) {
					var allFileNames = Object.keys(zip.files);
					// find all the files in the zip achive that have a 'WAD' extention in their filename
					var allWadFileNames = allFileNames.filter(function(fileName) { return fileName.split('.').pop().toUpperCase() === 'WAD'; });
					
					if (allWadFileNames.length > 0) { // If there's at least one .wad file in the zip file
						// Just process the first WAD file found
						// TODO: do something about zip files that contain multiple zip files
						var wadFileName = allWadFileNames[0]
						return zip.file(wadFileName).async('arraybuffer')
					} else {
						throw new Error('Provided zip file does not contain any files with a .wad extension');
					}
				},
				function (_reasonForError) {
					console.log("Error reading data as a zip file, so we're going to assume it's not a zip file")
					// assume that the error reading the file as a zip file means that it isn't actually a zip file,
					// and and so pass on the entire file as-is
					return dataBlob.arrayBuffer();
				}
			)
		}
		
		var requestData = function(event, onLoadFinish) {
			var timeBeforeRequest = (new Date()).getTime();

			var promiseOfWadData;
			var labelOfData;

			if (uriIsBeingDropped(event)) {	// links
				
				var uri = event.originalEvent.dataTransfer.getData("URL");	// this retrieves the first URL out of a possible list (https://developer.mozilla.org/en-US/docs/Web/Guide/HTML/Recommended_Drag_Types)

				var regex = /.*[/]([^/]*)$/;
				var found = uri.match(regex);
				var assetsLabel = found[1];

				labelOfData = assetsLabel;
				promiseOfWadData = requestWithGET(uri);
				
			} else if (fileIsBeingDropped(event)) {	// files on disk

				var files = event.originalEvent.dataTransfer.files; // Array of all files

				// Just process the first file
				labelOfData = files[0].name
				promiseOfWadData = new Promise((resolve, _reject) => {
					resolve(files[0]);
				});
			}

			promiseOfWadData
				.then(possiblyUnzipAndConverToArraybuffer)
				.then(parseWadData)
				.then(
					function (parsedData) {
						onLoadFinish();
						var timeAfterRequest = (new Date()).getTime();
						displayDoomAssets(parsedData, timeAfterRequest - timeBeforeRequest, labelOfData);
					},
					function (_errorReason) {
						onLoadFinish();
						var timeAfterRequest = (new Date()).getTime();
						displayDoomAssets("Error occurred with this reason: " + _errorReason, timeAfterRequest - timeBeforeRequest);
					}
				);
		};
		
		var uriLabel = getLabelFromDndEvent(event);
		
		var targetOfDrop = event.currentTarget;
	
		$(targetOfDrop).removeClass("dropIsValid")
		$(targetOfDrop).addClass("dropIsBeingProcessed")
		
		var maxLengthForUriLabel = 30;
		var labelForLoading = "Processing \"" + uriLabel.substring(0, maxLengthForUriLabel) + (uriLabel.length > maxLengthForUriLabel ? "..." : "") + "\" ...";
		
		var dropAreaContentsWhileLoading = document.createElement("div");
		dropAreaContentsWhileLoading.className = "contents";
		var loadIcon = document.createElement("img");
		loadIcon.className = "icon";
		loadIcon.id = "loadIcon";
		loadIcon.src = loadingImage;
		var loadLabel = document.createElement("div");
		loadLabel.id = "label";
		
		var firstLineSpan = document.createElement("span");
		firstLineSpan.innerHTML = labelForLoading;
		var secondLineSpan = document.createElement("span");
		secondLineSpan.innerHTML = "You've waited at least ";
		var waitReportingSpan = document.createElement("span");
		waitReportingSpan.innerHTML = "? seconds";
		loadLabel.appendChild(firstLineSpan);
		loadLabel.appendChild(document.createElement("br"));
		loadLabel.appendChild(secondLineSpan);
		loadLabel.appendChild(waitReportingSpan);
		
		dropAreaContentsWhileLoading.appendChild(loadIcon);
		dropAreaContentsWhileLoading.appendChild(loadLabel);
		
		var removedContents = $(targetOfDrop).children(".inner").children(".contents").replaceWith(dropAreaContentsWhileLoading)
		
		var timeAtRequest = (new Date()).getTime();
		
		var timerId = setInterval(function() {
			var currentTime = (new Date()).getTime();
			var secondsSpentWaiting = Math.round((currentTime - timeAtRequest) / 1000);
			var secondsLabel = (secondsSpentWaiting == 0) ? '?' : secondsSpentWaiting;
			waitReportingSpan.innerHTML = secondsLabel + " second" + (secondsSpentWaiting == 1 ? "" : "s");
		}, 500)
		
		// change the drop area to reflect that we're waiting for the load to finish
		
		var onLoadFinish = function() {
			$(targetOfDrop).children(".inner").children(".contents").replaceWith(removedContents);
			$(targetOfDrop).removeClass("dropIsBeingProcessed");
			clearTimeout(timerId);
		}
	
		event.preventDefault();
		
		requestData(event, onLoadFinish);
	}
}